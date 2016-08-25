/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

import net.link.util.InternalInconsistencyException;


public enum LinkIDCountry {

    Afghanistan( "AF", "AFG", "004" ), Aland_Islands( "AX", "ALA", "248" ), Albania( "AL", "ALB", "008" ), Algeria( "DZ", "DZA", "012" ), American_Samoa( "AS",
            "ASM", "016" ), Andorra( "AD", "AND", "020" ), Angola( "AO", "AGO", "024" ), Anguilla( "AI", "AIA", "660" ), Antarctica( "AQ", "ATA",
            "010" ), Antigua_and_Barbuda( "AG", "ATG", "028" ), Argentina( "AR", "ARG", "032" ), Armenia( "AM", "ARM", "051" ), Aruba( "AW", "ABW",
            "533" ), Australia( "AU", "AUS", "036" ), Austria( "AT", "AUT", "040" ), Azerbaijan( "AZ", "AZE", "031" ), Bahamas( "BS", "BHS", "044" ), Bahrain(
            "BH", "BHR", "048" ), Bangladesh( "BD", "BGD", "050" ), Barbados( "BB", "BRB", "052" ), Belarus( "BY", "BLR", "112" ), Belgium( "BE", "BEL",
            "056" ), Belize( "BZ", "BLZ", "084" ), Benin( "BJ", "BEN", "204" ), Bermuda( "BM", "BMU", "060" ), Bhutan( "BT", "BTN", "064" ), Bolivia( "BO",
            "BOL", "068" ), Bosnia_and_Herzegovina( "BA", "BIH", "070" ), Botswana( "BW", "BWA", "072" ), Bouvet_Island( "BV", "BVT", "074" ), Brazil( "BR",
            "BRA", "076" ), British_Virgin_Islands( "VG", "VGB", "092" ), British_Indian_Ocean_Territory( "IO", "IOT", "086" ), Brunei_Darussalam( "BN", "BRN",
            "096" ), Bulgaria( "BG", "BGR", "100" ), Burkina_Faso( "BF", "BFA", "854" ), Burundi( "BI", "BDI", "108" ), Cambodia( "KH", "KHM",
            "116" ), Cameroon( "CM", "CMR", "120" ), Canada( "CA", "CAN", "124" ), Cape_Verde( "CV", "CPV", "132" ), Cayman_Islands( "KY", "CYM",
            "136" ), Central_African_Republic( "CF", "CAF", "140" ), Chad( "TD", "TCD", "148" ), Chile( "CL", "CHL", "152" ), China( "CN", "CHN",
            "156" ), Hong_Kong( "HK", "HKG", "344" ), Macao( "MO", "MAC", "446" ), Christmas_Island( "CX", "CXR", "162" ), Cocos_Islands( "CC", "CCK",
            "166" ), Colombia( "CO", "COL", "170" ), Comoros( "KM", "COM", "174" ), Congo_Brazzaville( "CG", "COG", "178" ), Congo( "CD", "COD",
            "180" ), Cook_Islands( "CK", "COK", "184" ), Costa_Rica( "CR", "CRI", "188" ), Cote_dIvoire( "CI", "CIV", "384" ), Croatia( "HR", "HRV",
            "191" ), Cuba( "CU", "CUB", "192" ), Cyprus( "CY", "CYP", "196" ), Czech_Republic( "CZ", "CZE", "203" ), Denmark( "DK", "DNK", "208" ), Djibouti(
            "DJ", "DJI", "262" ), Dominica( "DM", "DMA", "212" ), Dominican_Republic( "DO", "DOM", "214" ), Ecuador( "EC", "ECU", "218" ), Egypt( "EG", "EGY",
            "818" ), El_Salvador( "SV", "SLV", "222" ), Equatorial_Guinea( "GQ", "GNQ", "226" ), Eritrea( "ER", "ERI", "232" ), Estonia( "EE", "EST",
            "233" ), Ethiopia( "ET", "ETH", "231" ), Falkland_Islands( "FK", "FLK", "238" ), Faroe_Islands( "FO", "FRO", "234" ), Fiji( "FJ", "FJI",
            "242" ), Finland( "FI", "FIN", "246" ), France( "FR", "FRA", "250" ), French_Guiana( "GF", "GUF", "254" ), French_Polynesia( "PF", "PYF",
            "258" ), French_Southern_Territories( "TF", "ATF", "260" ), Gabon( "GA", "GAB", "266" ), Gambia( "GM", "GMB", "270" ), Georgia( "GE", "GEO",
            "268" ), Germany( "DE", "DEU", "276" ), Ghana( "GH", "GHA", "288" ), Gibraltar( "GI", "GIB", "292" ), Greece( "GR", "GRC", "300" ), Greenland( "GL",
            "GRL", "304" ), Grenada( "GD", "GRD", "308" ), Guadeloupe( "GP", "GLP", "312" ), Guam( "GU", "GUM", "316" ), Guatemala( "GT", "GTM",
            "320" ), Guernsey( "GG", "GGY", "831" ), Guinea( "GN", "GIN", "324" ), Guinea_Bissau( "GW", "GNB", "624" ), Guyana( "GY", "GUY", "328" ), Haiti(
            "HT", "HTI", "332" ), HeardIslandMcdonaldIslands( "HM", "HMD", "334" ), Vatican_City_state( "VA", "VAT", "336" ), Honduras( "HN", "HND",
            "340" ), Hungary( "HU", "HUN", "348" ), Iceland( "IS", "ISL", "352" ), India( "IN", "IND", "356" ), Indonesia( "ID", "IDN", "360" ), Iran( "IR",
            "IRN", "364" ), Iraq( "IQ", "IRQ", "368" ), Ireland( "IE", "IRL", "372" ), Isle_of_Man( "IM", "IMN", "833" ), Israel( "IL", "ISR", "376" ), Italy(
            "IT", "ITA", "380" ), Jamaica( "JM", "JAM", "388" ), Japan( "JP", "JPN", "392" ), Jersey( "JE", "JEY", "832" ), Jordan( "JO", "JOR",
            "400" ), Kazakhstan( "KZ", "KAZ", "398" ), Kenya( "KE", "KEN", "404" ), Kiribati( "KI", "KIR", "296" ), North_Korea( "KP", "PRK",
            "408" ), South_Korea( "KR", "KOR", "410" ), Kuwait( "KW", "KWT", "414" ), Kyrgyzstan( "KG", "KGZ", "417" ), Lao( "LA", "LAO", "418" ), Latvia( "LV",
            "LVA", "428" ), Lebanon( "LB", "LBN", "422" ), Lesotho( "LS", "LSO", "426" ), Liberia( "LR", "LBR", "430" ), Libyan_Arab_Jamahiriya( "LY", "LBY",
            "434" ), Liechtenstein( "LI", "LIE", "438" ), Lithuania( "LT", "LTU", "440" ), Luxembourg( "LU", "LUX", "442" ), Macedonia( "MK", "MKD",
            "807" ), Madagascar( "MG", "MDG", "450" ), Malawi( "MW", "MWI", "454" ), Malaysia( "MY", "MYS", "458" ), Maldives( "MV", "MDV", "462" ), Mali( "ML",
            "MLI", "466" ), Malta( "MT", "MLT", "470" ), Marshall_Islands( "MH", "MHL", "584" ), Martinique( "MQ", "MTQ", "474" ), Mauritania( "MR", "MRT",
            "478" ), Mauritius( "MU", "MUS", "480" ), Mayotte( "YT", "MYT", "175" ), Mexico( "MX", "MEX", "484" ), Micronesia( "FM", "FSM", "583" ), Moldova(
            "MD", "MDA", "498" ), Monaco( "MC", "MCO", "492" ), Mongolia( "MN", "MNG", "496" ), Montenegro( "ME", "MNE", "499" ), Montserrat( "MS", "MSR",
            "500" ), Morocco( "MA", "MAR", "504" ), Mozambique( "MZ", "MOZ", "508" ), Myanmar( "MM", "MMR", "104" ), Namibia( "NA", "NAM", "516" ), Nauru( "NR",
            "NRU", "520" ), Nepal( "NP", "NPL", "524" ), Netherlands( "NL", "NLD", "528" ), Netherlands_Antilles( "AN", "ANT", "530" ), New_Caledonia( "NC",
            "NCL", "540" ), New_Zealand( "NZ", "NZL", "554" ), Nicaragua( "NI", "NIC", "558" ), Niger( "NE", "NER", "562" ), Nigeria( "NG", "NGA",
            "566" ), Niue( "NU", "NIU", "570" ), Norfolk_Island( "NF", "NFK", "574" ), Northern_Mariana_Islands( "MP", "MNP", "580" ), Norway( "NO", "NOR",
            "578" ), Oman( "OM", "OMN", "512" ), Pakistan( "PK", "PAK", "586" ), Palau( "PW", "PLW", "585" ), Palestinian_Territory( "PS", "PSE",
            "275" ), Panama( "PA", "PAN", "591" ), Papua_New_Guinea( "PG", "PNG", "598" ), Paraguay( "PY", "PRY", "600" ), Peru( "PE", "PER",
            "604" ), Philippines( "PH", "PHL", "608" ), Pitcairn( "PN", "PCN", "612" ), Poland( "PL", "POL", "616" ), Portugal( "PT", "PRT",
            "620" ), Puerto_Rico( "PR", "PRI", "630" ), Qatar( "QA", "QAT", "634" ), Reunion( "RE", "REU", "638" ), Romania( "RO", "ROU",
            "642" ), Russian_Federation( "RU", "RUS", "643" ), Rwanda( "RW", "RWA", "646" ), Saint_Barthelemy( "BL", "BLM", "652" ), Saint_Helena( "SH", "SHN",
            "654" ), Saint_Kitts_and_Nevis( "KN", "KNA", "659" ), Saint_Lucia( "LC", "LCA", "662" ), Saint_Martin( "MF", "MAF",
            "663" ), Saint_Pierre_and_Miquelon( "PM", "SPM", "666" ), Saint_Vincent_and_Grenadines( "VC", "VCT", "670" ), Samoa( "WS", "WSM",
            "882" ), San_Marino( "SM", "SMR", "674" ), Sao_Tome_and_Principe( "ST", "STP", "678" ), Saudi_Arabia( "SA", "SAU", "682" ), Senegal( "SN", "SEN",
            "686" ), Serbia( "RS", "SRB", "688" ), Seychelles( "SC", "SYC", "690" ), Sierra_Leone( "SL", "SLE", "694" ), Singapore( "SG", "SGP",
            "702" ), Slovakia( "SK", "SVK", "703" ), Slovenia( "SI", "SVN", "705" ), Solomon_Islands( "SB", "SLB", "090" ), Somalia( "SO", "SOM",
            "706" ), South_Africa( "ZA", "ZAF", "710" ), South_Georgia( "GS", "SGS", "239" ), Spain( "ES", "ESP", "724" ), Sri_Lanka( "LK", "LKA",
            "144" ), Sudan( "SD", "SDN", "736" ), Suriname( "SR", "SUR", "740" ), Svalbard_and_Jan_Mayen_Islands( "SJ", "SJM", "744" ), Swaziland( "SZ", "SWZ",
            "748" ), Sweden( "SE", "SWE", "752" ), Switzerland( "CH", "CHE", "756" ), Syrian_Arab_Republic( "SY", "SYR", "760" ), Taiwan( "TW", "TWN",
            "158" ), Tajikistan( "TJ", "TJK", "762" ), Tanzania( "TZ", "TZA", "834" ), Thailand( "TH", "THA", "764" ), Timor_Leste( "TL", "TLS", "626" ), Togo(
            "TG", "TGO", "768" ), Tokelau( "TK", "TKL", "772" ), Tonga( "TO", "TON", "776" ), Trinidad_and_Tobago( "TT", "TTO", "780" ), Tunisia( "TN", "TUN",
            "788" ), Turkey( "TR", "TUR", "792" ), Turkmenistan( "TM", "TKM", "795" ), Turks_and_Caicos_Islands( "TC", "TCA", "796" ), Tuvalu( "TV", "TUV",
            "798" ), Uganda( "UG", "UGA", "800" ), Ukraine( "UA", "UKR", "804" ), United_Arab_Emirates( "AE", "ARE", "784" ), United_Kingdom( "GB", "GBR",
            "826" ), USA( "US", "USA", "840" ), UnitedStatesMinorOutlyingIslands( "UM", "UMI", "581" ), Uruguay( "UY", "URY", "858" ), Uzbekistan( "UZ", "UZB",
            "860" ), Vanuatu( "VU", "VUT", "548" ), Venezuela( "VE", "VEN", "862" ), Vietnam( "VN", "VNM", "704" ), Virgin_Islands_British( "VG", "VGB",
            "092" ), Virgin_Islands_US( "VI", "VIR", "850" ), Wallis_and_Futuna_Islands( "WF", "WLF", "876" ), Western_Sahara( "EH", "ESH", "732" ), Yemen(
            "YE", "YEM", "887" ), Zambia( "ZM", "ZMB", "894" ), Zimbabwe( "ZW", "ZWE", "716" );

    // the ISO-3166 alpha-2 code
    private final String isoAlpha2Code;

    // the ISO-3166 alpha-3 code
    private final String isoAlpha3Code;

    // the ISO-3116 numeric code
    private final String isoNumericCode;

    LinkIDCountry(final String isoAlpha2Code, final String isoAlpha3Code, final String isoNumericCode) {

        this.isoAlpha2Code = isoAlpha2Code;
        this.isoAlpha3Code = isoAlpha3Code;
        this.isoNumericCode = isoNumericCode;
    }

    public String getIsoAlpha2Code() {

        return isoAlpha2Code;
    }

    public String getIsoAlpha3Code() {

        return isoAlpha3Code;
    }

    @SuppressWarnings("unused")
    public String getIsoNumericCode() {

        return isoNumericCode;
    }

    @Override
    public String toString() {

        return isoAlpha2Code;
    }

    public static LinkIDCountry toCountryAlpha2(String isoAlpha2Code) {

        if (null == isoAlpha2Code) {
            return Belgium;
        }

        for (LinkIDCountry linkIDCountry : LinkIDCountry.values()) {
            if (linkIDCountry.getIsoAlpha2Code().equals( isoAlpha2Code )) {
                return linkIDCountry;
            }
        }

        throw new InternalInconsistencyException( String.format( "Invalid country: %s", isoAlpha2Code ) );
    }
}
