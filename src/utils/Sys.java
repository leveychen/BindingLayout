package utils;

import com.google.common.base.CaseFormat;

/**
 * Created by levey on 2018/3/22.
 * sout.
 */
public final class Sys {

    public static final boolean DEBUG = true;

    public static void out(String s){
        if(DEBUG){
            System.out.println(s);
        }
    }

    public static String getXmlFileName(String className){
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClzName(className));
    }

    public static String getTypeName(String className){
        return toLowerCaseFirstOne(getClzName(className));
    }


    private static String getClzName(String className){
        if(className.endsWith(".java")){
            className = className.replaceAll(".java","");
        }
        if(className.endsWith(".JAVA")){
            className = className.replaceAll(".JAVA","");
        }
        return className;
    }

    private static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }



    static final String BASE_OBSERVABLE = "BaseObservable";
    static final String BINDABLE = "Bindable";
}
