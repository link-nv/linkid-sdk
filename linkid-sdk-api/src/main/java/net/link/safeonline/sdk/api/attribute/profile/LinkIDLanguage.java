/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

public enum LinkIDLanguage {

    Afar( "aa" ),
    Abkhazian( "ab" ),
    Afrikaans( "af" ),
    Akan( "ak" ),
    Amharic( "am" ),
    Arabic( "ar" ),
    Aragonese( "an" ),
    Armenian( "hy" ),
    Assamese( "as" ),
    Avaric( "av" ),
    Avestan( "ae" ),
    Aymara( "ay" ),
    Azerbaijani( "az" ),
    Bashkir( "ba" ),
    Bambara( "bm" ),
    Belarusian( "be" ),
    Bengali( "bn" ),
    Bihari_languages( "bh" ),
    Bislama( "bi" ),
    Tibetan( "bo" ),
    Bosnian( "bs" ),
    Breton( "br" ),
    Bulgarian( "bg" ),
    Catalan_Valencian( "ca" ),
    Czech( "cs" ),
    Chamorro( "ch" ),
    Chechen( "ce" ),
    Chinese( "zh" ),
    Church_lavic( "cu" ),
    Chuvash( "cv" ),
    Cornish( "kw" ),
    Corsican( "co" ),
    Cree( "cr" ),
    Danish( "da" ),
    German( "de" ),
    Divehi( "dv" ),
    Dutch( "nl" ),
    Dzongkha( "dz" ),
    Greek_Modern( "el" ),
    English( "en" ),
    Esperanto( "eo" ),
    Estonian( "et" ),
    Basque( "eu" ),
    Ewe( "ee" ),
    Faroese( "fo" ),
    Persian( "fa" ),
    Fijian( "fj" ),
    Finnish( "fi" ),
    French( "fr" ),
    Western_Frisian( "fy" ),
    Fulah( "ff" ),
    Gaelic( "gd" ),
    Irish( "ga" ),
    Galician( "gl" ),
    Manx( "gv" ),
    Guarani( "gn" ),
    Gujarati( "gu" ),
    Haitian( "ht" ),
    Hausa( "ha" ),
    Hebrew( "he" ),
    Herero( "hz" ),
    Hindi( "hi" ),
    Hiri_Motu( "ho" ),
    Croatian( "hr" ),
    Hungarian( "hu" ),
    Igbo( "ig" ),
    Icelandic( "is" ),
    Ido( "io" ),
    Sichuan_Yi( "ii" ),
    Inuktitut( "iu" ),
    Interlingue( "ie" ),
    Interlingua( "ia" ),
    Indonesian( "id" ),
    Inupiaq( "ik" ),
    Italian( "it" ),
    Javanese( "jv" ),
    Japanese( "ja" ),
    Kalaallisut( "kl" ),
    Kannada( "kn" ),
    Kashmiri( "ks" ),
    Georgian( "ka" ),
    Kanuri( "kr" ),
    Kazakh( "kk" ),
    Central_Khmer( "km" ),
    Kikuyu( "ki" ),
    Kinyarwanda( "rw" ),
    Kirghiz( "ky" ),
    Komi( "kv" ),
    Kongo( "kg" ),
    Korean( "ko" ),
    Kuanyama( "kj" ),
    Kurdish( "ku" ),
    Lao( "lo" ),
    Latin( "la" ),
    Latvian( "lv" ),
    Limburgan( "li" ),
    Lingala( "ln" ),
    Lithuanian( "lt" ),
    Luxembourgish( "lb" ),
    Luba_Katanga( "lu" ),
    Ganda( "lg" ),
    Marshallese( "mh" ),
    Malayalam( "ml" ),
    Maori( "mi" ),
    Marathi( "mr" ),
    Malay( "ms" ),
    Macedonian( "mk" ),
    Malagasy( "mg" ),
    Maltese( "mt" ),
    Mongolian( "mn" ),
    Burmese( "my" ),
    Nauru( "na" ),
    Navajo( "nv" ),
    Ndebele_South( "nr" ),
    Ndebele_north( "nd" ),
    Ndonga( "ng" ),
    Nepali( "ne" ),
    Norwegian_Nynorsk( "nn" ),
    Norwegian_Bokmal( "nb" ),
    Norwegian( "no" ),
    Chichewa( "ny" ),
    Occitan( "oc" ),
    Ojibwa( "oj" ),
    Oriya( "or" ),
    Oromo( "om" ),
    Ossetian( "os" ),
    Panjabi( "pa" ),
    Pali( "pi" ),
    Polish( "pl" ),
    Portuguese( "pt" ),
    Pushto( "ps" ),
    Quechua( "qu" ),
    Romansh( "rm" ),
    Romanian( "ro" ),
    Rundi( "rn" ),
    Russian( "ru" ),
    Sango( "sg" ),
    Sanskrit( "sa" ),
    Sinhala( "si" ),
    Slovak( "sk" ),
    Slovenian( "sl" ),
    Northern_Sami( "se" ),
    Samoan( "sm" ),
    Shona( "sn" ),
    Sindhi( "sd" ),
    Somali( "so" ),
    Sotho( "st" ),
    Spanish( "es" ),
    Albanian( "sq" ),
    Sardinian( "sc" ),
    Serbian( "sr" ),
    Swati( "ss" ),
    Sundanese( "su" ),
    Swahili( "sw" ),
    Swedish( "sv" ),
    Tahitian( "ty" ),
    Tamil( "ta" ),
    Tatar( "tt" ),
    Telugu( "te" ),
    Tajik( "tg" ),
    Tagalog( "tl" ),
    Thai( "th" ),
    Tigrinya( "ti" ),
    Tonga( "to" ),
    Tswana( "tn" ),
    Tsonga( "ts" ),
    Turkmen( "tk" ),
    Turkish( "tr" ),
    Twi( "tw" ),
    Uighur( "ug" ),
    Ukrainian( "uk" ),
    Urdu( "ur" ),
    Uzbek( "uz" ),
    Venda( "ve" ),
    Vietnamese( "vi" ),
    Volapuk( "vo" ),
    Welsh( "cy" ),
    Walloon( "wa" ),
    Wolof( "wo" ),
    Xhosa( "xh" ),
    Yiddish( "yi" ),
    Yoruba( "yo" ),
    Zhuang( "za" ),
    Zulu( "zu" );

    // the ISO-639-1 code
    private final String isoCode;

    LinkIDLanguage(final String isoCode) {

        this.isoCode = isoCode;
    }

    public String getIsoCode() {

        return isoCode;
    }

    public static LinkIDLanguage to(String isoCode) {

        for (LinkIDLanguage linkIDLanguage : LinkIDLanguage.values()) {
            if (linkIDLanguage.getIsoCode().equals( isoCode )) {
                return linkIDLanguage;
            }
        }

        throw new RuntimeException( String.format( "Invalid language: %s", isoCode ) );
    }
}
