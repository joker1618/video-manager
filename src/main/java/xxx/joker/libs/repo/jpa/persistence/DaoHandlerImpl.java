package xxx.joker.libs.repo.jpa.persistence;

import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkFormatter;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkReflection;
import xxx.joker.libs.core.runtime.wrapper.TypeWrapper;
import xxx.joker.libs.repo.config.RepoCtx;
import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.jpa.proxy.ProxyList;
import xxx.joker.libs.repo.jpa.proxy.ProxyMap;
import xxx.joker.libs.repo.jpa.proxy.ProxySet;
import xxx.joker.libs.repo.wrapper.RepoWClazz;
import xxx.joker.libs.repo.wrapper.RepoWField;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.core.format.csv.CsvConst.SEP_FIELD;
import static xxx.joker.libs.core.util.JkConvert.*;
import static xxx.joker.libs.core.util.JkStrings.strf;

class DaoHandlerImpl implements DaoHandler {

    private final RepoCtx ctx;
    private JkFormatter fmtEntities;
    private JkFormatter fmtGeneric;

    DaoHandlerImpl(RepoCtx ctx) {
        this.ctx = ctx;
        this.fmtEntities = JkFormatter.get();
        this.fmtGeneric = JkFormatter.get();
        initFormatters();
    }
    private void initFormatters() {
        for (RepoWClazz cw : ctx.getWClazzMap().values()) {
            fmtEntities.setCustomFormat("PK", obj -> ((RepoEntity)obj).getPrimaryKey());
            for (RepoWField fw : cw.getFields(RepoWField::isEntityFlat)) {
                fmtEntities.setFieldParse(fw.getField(), s -> null);
                if(fw.isEntity()) {
                    // RepoEntity: print ID on format, exclude in parsing (use FK file)
                    fmtEntities.setFieldFormat(fw.getField(), e -> String.valueOf(((RepoEntity)e).getEntityId()));
                } else if(fw.isList()) {
                    // Collection RepoEntity: print num elements, exclude in parsing (use FK file)
                    fmtEntities.setFieldFormat(fw.getField(), e -> {
                        ProxyList proxyList = (ProxyList) Proxy.getInvocationHandler(e);
                        return strf("({})", proxyList.getSourceList().size());
                    });
                } else if(fw.isSet()) {
                    // Collection RepoEntity: print num elements, exclude in parsing (use FK file)
                    fmtEntities.setFieldFormat(fw.getField(), e -> {
                        ProxySet proxySet = (ProxySet) Proxy.getInvocationHandler(e);
                        return strf("({})", proxySet.getSourceSet().size());
                    });
                } else if(fw.isMap()) {
                    // Map of flat RepoEntity: print num elements, exclude in parsing (use FK file)
                    fmtEntities.setFieldFormat(fw.getField(), e -> {
                        ProxyMap proxyMap = (ProxyMap) Proxy.getInvocationHandler(e);
                        return strf("({})", proxyMap.getSourceMap().size());
                    });
                }
            }
            for (RepoWField fw : cw.getFields(RepoWField::isResourcePath)) {
                fmtEntities.setFieldParse(fw.getField(), s -> {
                    Path subPath = Paths.get(s);
                    return subPath.isAbsolute() ? subPath : ctx.getResourcesFolder().resolve(subPath);
                });
                fmtEntities.setFieldFormat(fw.getField(), o -> {
                    Path p = (Path) o;
                    if(!p.isAbsolute()) {
                        return p.toString();
                    }
                    if(p.startsWith(ctx.getResourcesFolder())) {
                        return ctx.getResourcesFolder().relativize(p).toString();
                    }
                    return p.toString();
                });
            }
        }
    }

    @Override
    public List<DaoDTO> readData() {
        // Read entities data
        List<RepoEntity> entities = new ArrayList<>();
        for (RepoWClazz cw : ctx.getWClazzMap().values()) {
            Path dbPath = ctx.getDbPath(cw);
            if(Files.exists(dbPath)) {
                entities.addAll(fmtEntities.parseCsv(dbPath, cw.getEClazz(), true, cw.getFieldNames()));
            }
        }
        Map<Long, RepoEntity> entityMap = JkStreams.toMapSingle(entities, RepoEntity::getEntityId);

        // Read foreign keys
        Map<Long, List<DaoFK>> fkMap = new HashMap<>();
        if(Files.exists(ctx.getForeignKeysPath())) {
            List<DaoFK> fkList = fmtGeneric.parseCsv(ctx.getForeignKeysPath(), DaoFK.class);
            fkMap = JkStreams.toMap(fkList, DaoFK::getSourceID);
        }

        // Create DTOs
        List<DaoDTO> dtoList = new ArrayList<>();
        for (RepoEntity e : entities) {
            DaoDTO dto = new DaoDTO(e);
            dtoList.add(dto);
            List<DaoFK> fkList = fkMap.get(e.getEntityId());
            if(fkList != null) {
                dto.getForeignKeys().addAll(fkList);
                Map<String, List<DaoFK>> byName = JkStreams.toMap(fkList, DaoFK::getFieldName);
                byName.forEach((fname,daofks) -> {
                    RepoWField fw = ctx.getWClazz(e.getClass()).getField(fname);

                    List<DaoFK> fks = JkStreams.sorted(daofks);
                    if(fw.isEntity()) {
                        RepoEntity dep = entityMap.get(fks.get(0).getSingleDepID());
                        fw.setValue(e, dep);

                    } else if(fw.isCollection()) {
                        List<RepoEntity> depList = new ArrayList<>();
                        for (DaoFK fk : JkStreams.sorted(fks)) {
                            RepoEntity dep = entityMap.get(fk.getCollectionDepID());
                            depList.add(dep);
                        }
                        Collection<RepoEntity> coll = fw.isList() ? depList : toLinkedHashSet(depList);
                        fw.setValue(e, coll);

                    } else if(fw.isMap()) {
                        Map<String, List<DaoFK>> byKeyMap = JkStreams.toMap(fks, fk -> fk.getMapKey().getValue());
                        TypeWrapper twKey = fw.getParamType(0);
                        TypeWrapper twValue = fw.getParamType(1);
                        Map finalMap = new LinkedHashMap();
                        byKeyMap.forEach((k,v) -> {
                            Object key;
                            if(twKey.instanceOf(RepoEntity.class)) {
                                key = entityMap.get(toLong(k));
                            } else {
                                key = fmtEntities.parseValue(k, twKey);
                            }
                            Object value;
                            if(twValue.isCollection()) {
                                List<Object> depList = new ArrayList<>();
                                for (DaoFK fk : JkStreams.sorted(v)) {
                                    if(twValue.instanceOfFlat(RepoEntity.class)) {
                                        depList.add(entityMap.get(fk.getMapValueAsID()));
                                    } else {
                                        depList.add(fmtEntities.parseValue(fk.getValue().getValue(), twValue.getParamType(0)));
                                    }
                                }
                                value = twValue.isList() ? depList : toLinkedHashSet(depList);
                            } else {
                                if(twValue.instanceOf(RepoEntity.class)) {
                                    value = entityMap.get(v.get(0).getMapValueAsID());
                                } else {
                                    value = fmtEntities.parseValue(v.get(0).getValue().getValue(), twValue);
                                }
                            }
                            finalMap.put(key, value);
                        });
                        fw.setValue(e, finalMap);
                    }
                });
            }
        }

        return dtoList;
    }

    @Override
    public boolean persistData(Collection<RepoEntity> entities) {
        // Persist entities data
        List<RepoEntity> idSorted = JkStreams.sorted(entities, Comparator.comparing(RepoEntity::getEntityId));
        Map<Class<? extends RepoEntity>, List<RepoEntity>> map = JkStreams.toMap(idSorted, RepoEntity::getClass);
        map.forEach((clazz, eList) -> {
            Path dbPath = ctx.getDbPath(clazz.getSimpleName());
            List<String> lines = fmtEntities.formatCsv(eList);
            JkFiles.writeFile(dbPath, lines);
        });

        // Persist empty file, with just the header, for the classes with no entities
        List<RepoWClazz> emptyWClass = JkStreams.filter(ctx.getWClazzMap().values(), wc -> !map.containsKey(wc.getEClazz()));
        emptyWClass.forEach(wc -> {
            String header = JkStreams.join(wc.getFields(), SEP_FIELD.getSeparator(), RepoWField::getFieldName);
            JkFiles.writeFile(ctx.getDbPath(wc), header);
        });

        // Persist foreign keys
        List<DaoFK> fkList = JkStreams.flatMap(createDTOs(entities), DaoDTO::getForeignKeys);
        if(!fkList.isEmpty()) {
            JkFiles.writeFile(ctx.getForeignKeysPath(), fmtGeneric.formatCsv(fkList));
        } else {
            String header = JkStreams.join(JkReflection.findAllFields(DaoFK.class), SEP_FIELD.getSeparator(), Field::getName);
            JkFiles.writeFile(ctx.getForeignKeysPath(), header);
        }

        return true;
    }

    @Override
    public List<DaoDTO> createDTOs(Collection<RepoEntity> entities) {
        List<DaoDTO> dtoList = new ArrayList<>();

        for (RepoEntity e : entities) {
            DaoDTO dto = new DaoDTO(e);
            dtoList.add(dto);
            RepoWClazz cw = ctx.getWClazz(e.getClass());
            for (RepoWField fw : cw.getFields(RepoWField::isEntityFlat)) {
                if(fw.isEntity()) {
                    RepoEntity dep = fw.getValue(e);
                    dto.getForeignKeys().add(DaoFK.ofSingle(e, fw, dep));

                } else if(fw.isCollection()) {
                    Collection<RepoEntity> coll = fw.getValue(e);
                    AtomicInteger counter = new AtomicInteger(0);
                    coll.forEach(dep -> dto.getForeignKeys().add(DaoFK.ofCollection(e, fw, dep, counter.getAndIncrement())));

                } else if(fw.isMap()) {
                    Map<?,?> map = fw.getValue(e);
                    AtomicInteger counterKey = new AtomicInteger(0);
                    map.forEach((key, value) -> {
                        int idxKey = counterKey.getAndIncrement();
                        if (fw.getParamType(1).isCollection()) {
                            Collection<?> coll = (Collection) value;
                            AtomicInteger counterValue = new AtomicInteger(0);
                            List<DaoFK> fkList = JkStreams.map(coll, elem -> DaoFK.ofMapColl(e, fw, idxKey, key, counterValue.getAndIncrement(), elem));
                            dto.getForeignKeys().addAll(fkList);
                        } else {
                            dto.getForeignKeys().add(DaoFK.ofMapSingle(e, fw, idxKey, key, value));
                        }
                    });
                }
            }
        }

        return dtoList;
    }
}
