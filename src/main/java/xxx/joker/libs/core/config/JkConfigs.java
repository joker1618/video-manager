package xxx.joker.libs.core.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Manage properties.
 *
 * In the property files is allowed the use of variables:
 * - environment variables:	${env:PROP_KEY}
 * - other property key:	${PROP_KEY}
 *
 * Every time a property is added, all the properties are re-evaluated to resolve the variables#
 *
 */

public class JkConfigs {

	private static final Logger LOG = LoggerFactory.getLogger(JkConfigs.class);

	private static JkConfigs appConfigs;

	protected Map<String, Prop> configMap;

	private static final String KEY_SEP = "=";
	private static final String COMMENT_START = "#";


	public JkConfigs() {
		this.configMap = Collections.synchronizedMap(new HashMap<>());
	}
	public JkConfigs(Path configFile) {
		this();
		loadConfigFile(configFile);
	}

	public static synchronized JkConfigs getAppConfigs() {
		if(appConfigs == null) {
			appConfigs = new JkConfigs();
		}
		return appConfigs;
	}

	public boolean loadConfigFile(Path configFile) {
		try {
			if (Files.exists(configFile)) {
				List<String> lines = JkFiles.readLines(configFile);
				addPropertiesFromFileLines(lines);
				return true;
			}
		} catch (Exception ex) {
			LOG.error("Unable to load properties from: "+configFile, ex);
		}
		return false;
	}
	public boolean loadConfigFile(String configFilePath) {
		return loadConfigFile(Paths.get(configFilePath));
	}
	public boolean loadConfigFile(InputStream is) {
		try {
			List<String> lines = JkFiles.readLines(is);
			addPropertiesFromFileLines(lines);
			return true;
		} catch (Exception ex) {
			LOG.error("Unable to load properties", ex);
			return false;
		}
	}
	private void addPropertiesFromFileLines(List<String> lines) {
		// read properties from file
		List<String> tmp = JkStreams.map(lines, String::trim);
		tmp.removeIf(StringUtils::isBlank);
		tmp.removeIf(line -> line.startsWith(COMMENT_START));
		tmp.removeIf(line -> !line.contains(KEY_SEP));

		for(String line : tmp) {
			int idxSep = line.indexOf(KEY_SEP);
			String key = line.substring(0, idxSep).trim();
			String value = line.substring(idxSep+1).trim();
			configMap.put(key, new Prop(key, value, value));
		}

		// replace environment variables  ${env:var}
		evaluateEnvironmentVariables();

		// replace variables ${var}
		evaluateVariables();
	}

	public void persist(Path outputPath) {
		List<String> lines = JkStreams.map(configMap.values(), p -> strf("%s=%s", p.key, p.evalutedValue));
		JkFiles.writeFile(outputPath, lines, true);
	}

	public String getString(String key) {
		return getString(key, null);
	}
	public String getString(String key, String _default) {
		Prop prop = configMap.get(key);
		return prop == null ? _default : prop.evalutedValue;
	}

	public Integer getInt(String key) {
		return getInt(key, null);
	}
	public Integer getInt(String key, Integer _default) {
		return JkConvert.toInt(getString(key), _default);
	}

	public Long getLong(String key) {
		return getLong(key, null);
	}
	public Long getLong(String key, Long _default) {
		return JkConvert.toLong(getString(key), _default);
	}

	public Double getDouble(String key) {
		return getDouble(key, null);
	}
	public Double getDouble(String key, Double _default) {
		return JkConvert.toDouble(getString(key), _default);
	}

	public BigDecimal getBigDecimal(String key) {
		return getBigDecimal(key, null);
	}
	public BigDecimal getBigDecimal(String key, BigDecimal _default) {
		String value = getString(key);
		if(value == null)	return _default;
		Double d = JkConvert.toDouble(value);
		return d == null ? _default : BigDecimal.valueOf(d);
	}

	public Path getPath(String key) {
		return Paths.get(getString(key));
	}
	public Path getPath(String key, Path _default) {
		String value = getString(key);
		return value == null ? _default : Paths.get(value);
	}

	public boolean getBoolean(String key) {
		return Boolean.valueOf(getString(key));
	}
	public boolean getBoolean(String key, boolean def) {
		String str = getString(key);
		return StringUtils.isBlank(str) ? def : Boolean.valueOf(str);
	}
	
	public void addProperty(String key, String value) {
		Prop prop = new Prop(key, value, value);
		configMap.put(key, prop);
		evaluateVariables();
	}

	public boolean containsKey(String key) {
		return configMap.containsKey(key);
	}

	@Override
	public String toString() {
		List<String> list = new ArrayList<>();
		configMap.forEach((k,v) -> list.add(k + "=" + v.evalutedValue));
		return list.stream().collect(Collectors.joining("\n"));
	}


	private void evaluateEnvironmentVariables() {
		for(Prop prop : configMap.values()) {
			String[] envVars = StringUtils.substringsBetween(prop.originalValue, "${env:", "}");
			if(envVars != null) {
				String fixed = prop.originalValue;
				for(String evar : envVars) {
					String strPh = "${env:" + evar + "}";
					String strRepl = JkStrings.safeTrim(System.getProperty(evar));
					fixed = fixed.replace(strPh, strRepl);
				}
				prop.originalValue = fixed;
			}
		}
	}
	private void evaluateVariables() {
		configMap.forEach((key,prop) -> prop.evalutedValue = prop.originalValue);

		Set<String> keys = configMap.keySet();
		boolean changed;
		do {
			changed = false;
			for(String key : keys) {
				Prop prop = configMap.get(key);
				String actualEval = prop.evalutedValue;
				if(containsVariables(actualEval)) {
					List<Var> vars = getVariables(actualEval);
					String newEval = actualEval;
					for (Var v : vars) {
						Prop p = configMap.get(v.varName);
						if(p != null) 	newEval = newEval.replace(v.placeholder, p.evalutedValue);

					}
					if(!newEval.equals(actualEval)) {
						prop.evalutedValue = newEval;
						changed = true;
					}
				}
			}
		} while (changed);
	}
	// return the next variable found:   ${var}
	private List<Var> getVariables(String value) {
		String str = value;
		boolean go = true;
		List<Var> toRet = new ArrayList<>();

		while(go) {
			String varName = StringUtils.substringBetween(str, "${", "}");
			if(StringUtils.isBlank(varName)) {
				go = false;
			} else {
				Var var = new Var(varName);
				toRet.add(var);
				int nextStart = str.indexOf(var.placeholder) + var.placeholder.length();
				str = str.substring(nextStart);
			}
		}

		return toRet;
	}
	private boolean containsVariables(String value) {
		String varName = StringUtils.substringBetween(value, "${", "}");
		return StringUtils.isNotBlank(varName);
	}

	private class Var {
		String varName;
		String placeholder;
		Var(String varName) {
			this.varName = varName;
			this.placeholder = "${" + varName + "}";
		}
	}

	private class Prop {
		String key;
		String originalValue;
		String evalutedValue;
		Prop(String key, String originalValue, String evalutedValue) {
			this.key = key;
			this.originalValue = originalValue;
			this.evalutedValue = evalutedValue;
		}
		public String toString() {
			return String.format("[%s, %s]", originalValue, evalutedValue);
		}

		public String getKey() {
			return key;
		}

		public String getOriginalValue() {
			return originalValue;
		}

		public String getEvalutedValue() {
			return evalutedValue;
		}
	}
}

