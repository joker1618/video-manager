package xxx.joker.libs.core.scanner;

import xxx.joker.libs.core.lambda.JkStreams;

import java.util.Arrays;
import java.util.List;

public class JkHtmlChars {

    private static final List<HtmlChar> CHAR_SHORT_LIST;
    private static final List<HtmlChar> CHAR_FULL_LIST;
    static {
        CHAR_SHORT_LIST = Arrays.asList(
//                new HtmlChar("&quot;", "\"", "&#34;"),
//                new HtmlChar("&num;", "#", "&#35;"),
//                new HtmlChar("&dollar;", "$", "&#36;"),
//                new HtmlChar("&percnt;", "%", "&#37;"),
//                new HtmlChar("&amp;", "&", "&#38;"),
                new HtmlChar("&apos;", "\'", "&#39;"),
//                new HtmlChar("&lpar;", "(", "&#40;"),
//                new HtmlChar("&rpar;", ")", "&#41;"),
//                new HtmlChar("&ast;", "*", "&#42;"),
//                new HtmlChar("&plus;", "+", "&#43;"),
//                new HtmlChar("&comma;", ",", "&#44;"),
//                new HtmlChar("&minus;", "-", "&#45;"),
//                new HtmlChar("&period;", ".", "&#46;"),
//                new HtmlChar("&sol;", "/", "&#47;"),
//                new HtmlChar("&colon;", ":", "&#58;"),
//                new HtmlChar("&semi;", ";", "&#59;"),
//                new HtmlChar("&lt;", "<", "&#60;"),
//                new HtmlChar("&equals;", "=", "&#61;"),
//                new HtmlChar("&gt;", ">", "&#62;"),
//                new HtmlChar("&quest;", "?", "&#63;"),
//                new HtmlChar("&commat;", "@", "&#64;"),
//                new HtmlChar("&lsqb;", "[", "&#91;"),
//                new HtmlChar("&bsol;", "\\", "&#92;"),
//                new HtmlChar("&rsqb;", "]", "&#93;"),
//                new HtmlChar("&Hat;", "^", "&#94;"),
//                new HtmlChar("&lowbar;", "_", "&#95;"),
//                new HtmlChar("&grave;", "`", "&#96;"),
//                new HtmlChar("&lcub;", "{", "&#123;"),
//                new HtmlChar("&verbar;", "|", "&#124;"),
//                new HtmlChar("&rcub;", "}", "&#125;"),
//                new HtmlChar("", "~", "&#126;"),
                new HtmlChar("&nbsp;", " ", "&#160;")
//                new HtmlChar("&iexcl;", "¡", "&#161;"),
//                new HtmlChar("&cent;", "¢", "&#162;"),
//                new HtmlChar("&pound;", "£", "&#163;"),
//                new HtmlChar("&curren;", "¤", "&#164;"),
//                new HtmlChar("&yen;", "¥", "&#165;"),
//                new HtmlChar("&brvbar;", "¦", "&#166;"),
//                new HtmlChar("&sect;", "§", "&#167;")
        );
    }

    public static String fixDirtyChars(String str) {
        String toRet = str;
        if(toRet.contains("&")) {
            for (HtmlChar hc : CHAR_SHORT_LIST) {
                toRet = toRet.replace(hc.andCode, hc.character).replace(hc.hashtagCode, hc.character);
            }
        }
        return toRet;
    }

    public static String escapeHtmlChars(String html) {
        for(HtmlChar hc : CHAR_FULL_LIST) {
            if(!hc.getAndCode().isEmpty()) {
                html = html.replace(hc.getAndCode(), hc.getStringChar());
            }
            if(!hc.getHashtagCode().isEmpty()) {
                html = html.replace(hc.getHashtagCode(), hc.getStringChar());
            }
        }
        return html;
    }

    public static List<String> escapeHtmlChars(List<String> source) {
        return JkStreams.map(source, JkHtmlChars::escapeHtmlChars);
    }

    private static class HtmlChar {
        private String andCode;
        private String hashtagCode;
        private String character;

        public HtmlChar() {
        }
		public HtmlChar(String andCode, String character, String hashtagCode) {
            this.andCode = andCode;
            this.hashtagCode = hashtagCode;
            this.character = character;
        }
		public String getAndCode() {
            return andCode;
        }
		public void setAndCode(String andCode) {
            this.andCode = andCode;
        }
		public String getHashtagCode() {
            return hashtagCode;
        }
		public void setHashtagCode(String hashtagCode) {
            this.hashtagCode = hashtagCode;
        }
		public String getCharacter() {
            return character;
        }
		public String getStringChar() {
            return String.valueOf(character);
        }
		public void setCharacter(String character) {
            this.character = character;
        }
    }

    static {
        CHAR_FULL_LIST = Arrays.asList(
                new HtmlChar("&quot;", "\"", "&#34;"),
                new HtmlChar("&num;", "#", "&#35;"),
                new HtmlChar("&dollar;", "$", "&#36;"),
                new HtmlChar("&percnt;", "%", "&#37;"),
                new HtmlChar("&amp;", "&", "&#38;"),
                new HtmlChar("&apos;", "\'", "&#39;"),
                new HtmlChar("&lpar;", "(", "&#40;"),
                new HtmlChar("&rpar;", ")", "&#41;"),
                new HtmlChar("&ast;", "*", "&#42;"),
                new HtmlChar("&plus;", "+", "&#43;"),
                new HtmlChar("&comma;", ",", "&#44;"),
                new HtmlChar("&minus;", "-", "&#45;"),
                new HtmlChar("&period;", ".", "&#46;"),
                new HtmlChar("&sol;", "/", "&#47;"),
                new HtmlChar("&colon;", ":", "&#58;"),
                new HtmlChar("&semi;", ";", "&#59;"),
                new HtmlChar("&lt;", "<", "&#60;"),
                new HtmlChar("&equals;", "=", "&#61;"),
                new HtmlChar("&gt;", ">", "&#62;"),
                new HtmlChar("&quest;", "?", "&#63;"),
                new HtmlChar("&commat;", "@", "&#64;"),
                new HtmlChar("&lsqb;", "[", "&#91;"),
                new HtmlChar("&bsol;", "\\", "&#92;"),
                new HtmlChar("&rsqb;", "]", "&#93;"),
                new HtmlChar("&Hat;", "^", "&#94;"),
                new HtmlChar("&lowbar;", "_", "&#95;"),
                new HtmlChar("&grave;", "`", "&#96;"),
                new HtmlChar("&lcub;", "{", "&#123;"),
                new HtmlChar("&verbar;", "|", "&#124;"),
                new HtmlChar("&rcub;", "}", "&#125;"),
                new HtmlChar("", "~", "&#126;"),
                new HtmlChar("&nbsp;", " ", "&#160;"),
                new HtmlChar("&iexcl;", "¡", "&#161;"),
                new HtmlChar("&cent;", "¢", "&#162;"),
                new HtmlChar("&pound;", "£", "&#163;"),
                new HtmlChar("&curren;", "¤", "&#164;"),
                new HtmlChar("&yen;", "¥", "&#165;"),
                new HtmlChar("&brvbar;", "¦", "&#166;"),
                new HtmlChar("&sect;", "§", "&#167;"),
                new HtmlChar("&uml;", "¨", "&#168;"),
                new HtmlChar("&copy;", "©", "&#169;"),
                new HtmlChar("&ordf;", "ª", "&#170;"),
                new HtmlChar("&laquo;", "«", "&#171;"),
                new HtmlChar("&not;", "¬", "&#172;"),
                new HtmlChar("&reg;", "®", "&#174;"),
                new HtmlChar("&macr;", "¯", "&#175;"),
                new HtmlChar("&deg;", "°", "&#176;"),
                new HtmlChar("&plusmn;", "±", "&#177;"),
                new HtmlChar("&sup2;", "²", "&#178;"),
                new HtmlChar("&sup3;", "³", "&#179;"),
                new HtmlChar("&acute;", "´", "&#180;"),
                new HtmlChar("&micro;", "µ", "&#181;"),
                new HtmlChar("&para;", "¶", "&#182;"),
                new HtmlChar("&middot;", "·", "&#183;"),
                new HtmlChar("&cedil;", "¸", "&#184;"),
                new HtmlChar("&sup1;", "¹", "&#185;"),
                new HtmlChar("&ordm;", "º", "&#186;"),
                new HtmlChar("&raquo;", "»", "&#187;"),
                new HtmlChar("&frac14;", "¼", "&#188;"),
                new HtmlChar("&frac12;", "½", "&#189;"),
                new HtmlChar("&frac34;", "¾", "&#190;"),
                new HtmlChar("&iquest;", "¿", "&#191;"),
                new HtmlChar("&Agrave;", "À", "&#192;"),
                new HtmlChar("&Aacute;", "Á", "&#193;"),
                new HtmlChar("&Acirc;", "Â", "&#194;"),
                new HtmlChar("&Atilde;", "Ã", "&#195;"),
                new HtmlChar("&Auml;", "Ä", "&#196;"),
                new HtmlChar("&Aring;", "Å", "&#197;"),
                new HtmlChar("&AElig;", "Æ", "&#198;"),
                new HtmlChar("&Ccedil;", "Ç", "&#199;"),
                new HtmlChar("&Egrave;", "È", "&#200;"),
                new HtmlChar("&Eacute;", "É", "&#201;"),
                new HtmlChar("&Ecirc;", "Ê", "&#202;"),
                new HtmlChar("&Euml;", "Ë", "&#203;"),
                new HtmlChar("&Igrave;", "Ì", "&#204;"),
                new HtmlChar("&Iacute;", "Í", "&#205;"),
                new HtmlChar("&Icirc;", "Î", "&#206;"),
                new HtmlChar("&Iuml;", "Ï", "&#207;"),
                new HtmlChar("&ETH;", "Ð", "&#208;"),
                new HtmlChar("&Ntilde;", "Ñ", "&#209;"),
                new HtmlChar("&Ograve;", "Ò", "&#210;"),
                new HtmlChar("&Oacute;", "Ó", "&#211;"),
                new HtmlChar("&Ocirc;", "Ô", "&#212;"),
                new HtmlChar("&Otilde;", "Õ", "&#213;"),
                new HtmlChar("&Ouml;", "Ö", "&#214;"),
                new HtmlChar("&times;", "×", "&#215;"),
                new HtmlChar("&Oslash;", "Ø", "&#216;"),
                new HtmlChar("&Ugrave;", "Ù", "&#217;"),
                new HtmlChar("&Uacute;", "Ú", "&#218;"),
                new HtmlChar("&Ucirc;", "Û", "&#219;"),
                new HtmlChar("&Uuml;", "Ü", "&#220;"),
                new HtmlChar("&Yacute;", "Ý", "&#221;"),
                new HtmlChar("&THORN;", "Þ", "&#222;"),
                new HtmlChar("&szlig;", "ß", "&#223;"),
                new HtmlChar("&agrave;", "à", "&#224;"),
                new HtmlChar("&aacute;", "á", "&#225;"),
                new HtmlChar("&acirc;", "â", "&#226;"),
                new HtmlChar("&atilde;", "ã", "&#227;"),
                new HtmlChar("&auml;", "ä", "&#228;"),
                new HtmlChar("&aring;", "å", "&#229;"),
                new HtmlChar("&aelig;", "æ", "&#230;"),
                new HtmlChar("&ccedil;", "ç", "&#231;"),
                new HtmlChar("&egrave;", "è", "&#232;"),
                new HtmlChar("&eacute;", "é", "&#233;"),
                new HtmlChar("&ecirc;", "ê", "&#234;"),
                new HtmlChar("&euml;", "ë", "&#235;"),
                new HtmlChar("&igrave;", "ì", "&#236;"),
                new HtmlChar("&iacute;", "í", "&#237;"),
                new HtmlChar("&icirc;", "î", "&#238;"),
                new HtmlChar("&iuml;", "ï", "&#239;"),
                new HtmlChar("&eth;", "ð", "&#240;"),
                new HtmlChar("&ntilde;", "ñ", "&#241;"),
                new HtmlChar("&ograve;", "ò", "&#242;"),
                new HtmlChar("&oacute;", "ó", "&#243;"),
                new HtmlChar("&ocirc;", "ô", "&#244;"),
                new HtmlChar("&otilde;", "õ", "&#245;"),
                new HtmlChar("&ouml;", "ö", "&#246;"),
                new HtmlChar("&divide;", "÷", "&#247;"),
                new HtmlChar("&oslash;", "ø", "&#248;"),
                new HtmlChar("&ugrave;", "ù", "&#249;"),
                new HtmlChar("&uacute;", "ú", "&#250;"),
                new HtmlChar("&ucirc;", "û", "&#251;"),
                new HtmlChar("&uuml;", "ü", "&#252;"),
                new HtmlChar("&yacute;", "ý", "&#253;"),
                new HtmlChar("&thorn;", "þ", "&#254;"),
                new HtmlChar("&yuml;", "ÿ", "&#255;"),
                new HtmlChar("&OElig;", "Œ", "&#338;"),
                new HtmlChar("&oelig;", "œ", "&#339;"),
                new HtmlChar("&Scaron;", "Š", "&#352;"),
                new HtmlChar("&scaron;", "š", "&#353;"),
                new HtmlChar("&Yuml;", "Ÿ", "&#376;"),
                new HtmlChar("&fnof;", "ƒ", "&#402;"),
                new HtmlChar("&Alpha;", "Α", "&#913;"),
                new HtmlChar("&Beta;", "Β", "&#914;"),
                new HtmlChar("&Gamma;", "Γ", "&#915;"),
                new HtmlChar("&Delta;", "Δ", "&#916;"),
                new HtmlChar("&Epsilon;", "Ε", "&#917;"),
                new HtmlChar("&Zeta;", "Ζ", "&#918;"),
                new HtmlChar("&Eta;", "Η", "&#919;"),
                new HtmlChar("&Theta;", "Θ", "&#920;"),
                new HtmlChar("&Iota;", "Ι", "&#921;"),
                new HtmlChar("&Kappa;", "Κ", "&#922;"),
                new HtmlChar("&Lambda;", "Λ", "&#923;"),
                new HtmlChar("&Mu;", "Μ", "&#924;"),
                new HtmlChar("&Nu;", "Ν", "&#925;"),
                new HtmlChar("&Xi;", "Ξ", "&#926;"),
                new HtmlChar("&Omicron;", "Ο", "&#927;"),
                new HtmlChar("&Pi;", "Π", "&#928;"),
                new HtmlChar("&Rho;", "Ρ", "&#929;"),
                new HtmlChar("&Sigma;", "Σ", "&#931;"),
                new HtmlChar("&Tau;", "Τ", "&#932;"),
                new HtmlChar("&Upsilon;", "Υ", "&#933;"),
                new HtmlChar("&Phi;", "Φ", "&#934;"),
                new HtmlChar("&Chi;", "Χ", "&#935;"),
                new HtmlChar("&Psi;", "Ψ", "&#936;"),
                new HtmlChar("&Omega;", "Ω", "&#937;"),
                new HtmlChar("&alpha;", "α", "&#945;"),
                new HtmlChar("&beta;", "β", "&#946;"),
                new HtmlChar("&gamma;", "γ", "&#947;"),
                new HtmlChar("&delta;", "δ", "&#948;"),
                new HtmlChar("&epsilon;", "ε", "&#949;"),
                new HtmlChar("&zeta;", "ζ", "&#950;"),
                new HtmlChar("&eta;", "η", "&#951;"),
                new HtmlChar("&theta;", "θ", "&#952;"),
                new HtmlChar("&iota;", "ι", "&#953;"),
                new HtmlChar("&kappa;", "κ", "&#954;"),
                new HtmlChar("&lambda;", "λ", "&#955;"),
                new HtmlChar("&mu;", "μ", "&#956;"),
                new HtmlChar("&nu;", "ν", "&#957;"),
                new HtmlChar("&xi;", "ξ", "&#958;"),
                new HtmlChar("&omicron;", "ο", "&#959;"),
                new HtmlChar("&pi;", "π", "&#960;"),
                new HtmlChar("&rho;", "ρ", "&#961;"),
                new HtmlChar("&sigmaf;", "ς", "&#962;"),
                new HtmlChar("&sigma;", "σ", "&#963;"),
                new HtmlChar("&tau;", "τ", "&#964;"),
                new HtmlChar("&upsilon;", "υ", "&#965;"),
                new HtmlChar("&phi;", "φ", "&#966;"),
                new HtmlChar("&chi;", "χ", "&#967;"),
                new HtmlChar("&psi;", "ψ", "&#968;"),
                new HtmlChar("&omega;", "ω", "&#969;"),
                new HtmlChar("&thetasym;", "ϑ", "&#977;"),
                new HtmlChar("&upsih;", "ϒ", "&#978;"),
                new HtmlChar("&piv;", "ϖ", "&#982;"),
                new HtmlChar("&ndash;", "–", "&#8211;"),
                new HtmlChar("&mdash;", "—", "&#8212;"),
                new HtmlChar("&lsquo;", "‘", "&#8216;"),
                new HtmlChar("&rsquo;", "’", "&#8217;"),
                new HtmlChar("&sbquo;", "‚", "&#8218;"),
                new HtmlChar("&ldquo;", "“", "&#8220;"),
                new HtmlChar("&rdquo;", "”", "&#8221;"),
                new HtmlChar("&bdquo;", "„", "&#8222;"),
                new HtmlChar("&dagger;", "†", "&#8224;"),
                new HtmlChar("&Dagger;", "‡", "&#8225;"),
                new HtmlChar("&permil;", "‰", "&#8240;"),
                new HtmlChar("&lsaquo;", "‹", "&#8249;"),
                new HtmlChar("&rsaquo;", "›", "&#8250;"),
                new HtmlChar("&bull;", "•", "&#8226;"),
                new HtmlChar("&hellip;", "…", "&#8230;"),
                new HtmlChar("&prime;", "′", "&#8242;"),
                new HtmlChar("&Prime;", "″", "&#8243;"),
                new HtmlChar("&oline;", "‾", "&#8254;"),
                new HtmlChar("&frasl;", "⁄", "&#8260;"),
                new HtmlChar("&trade;", "™", "&#8482;"),
                new HtmlChar("&larr;", "←", "&#8592;"),
                new HtmlChar("&uarr;", "↑", "&#8593;"),
                new HtmlChar("&rarr;", "→", "&#8594;"),
                new HtmlChar("&darr;", "↓", "&#8595;"),
                new HtmlChar("&harr;", "↔", "&#8596;"),
                new HtmlChar("&rArr;", "⇒", "&#8658;"),
                new HtmlChar("&hArr;", "⇔", "&#8660;"),
                new HtmlChar("", "◄", "&#9668;"),
                new HtmlChar("", "►", "&#9658;"),
                new HtmlChar("", "▲", "&#9650;"),
                new HtmlChar("", "▼", "&#9660;"),
                new HtmlChar("&forall;", "∀", "&#8704;"),
                new HtmlChar("&part;", "∂", "&#8706;"),
                new HtmlChar("&nabla;", "∇", "&#8711;"),
                new HtmlChar("&prod;", "∏", "&#8719;"),
                new HtmlChar("&sum;", "∑", "&#8721;"),
                new HtmlChar("&minus;", "−", "&#8722;"),
                new HtmlChar("&radic;", "√", "&#8730;"),
                new HtmlChar("&infin;", "∞", "&#8734;"),
                new HtmlChar("&and;", "∧", "&#8743;"),
                new HtmlChar("&or;", "∨", "&#8744;"),
                new HtmlChar("&cap;", "∩", "&#8745;"),
                new HtmlChar("&cup;", "∪", "&#8746;"),
                new HtmlChar("&int;", "∫", "&#8747;"),
                new HtmlChar("&there4;", "∴", "&#8756;"),
                new HtmlChar("&asymp;", "≈", "&#8776;"),
                new HtmlChar("&ne;", "≠", "&#8800;"),
                new HtmlChar("&equiv;", "≡", "&#8801;"),
                new HtmlChar("&le;", "≤", "&#8804;"),
                new HtmlChar("&ge;", "≥", "&#8805;"),
                new HtmlChar("&perp;", "⊥", "&#8869;"),
                new HtmlChar("&lang;", "〈", "&#9001;"),
                new HtmlChar("&rang;", "〉", "&#9002;"),
                new HtmlChar("&loz;", "◊", "&#9674;"),
                new HtmlChar("&spades;", "♠", "&#9824;"),
                new HtmlChar("&clubs;", "♣", "&#9827;"),
                new HtmlChar("&hearts;", "♥", "&#9829;"),
                new HtmlChar("&diams;", "♦", "&#9830;")
        );
    }
}
