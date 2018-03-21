package utils;

import com.google.common.base.CaseFormat;

/**
 * Created by levey on 2018/3/21.
 * sout.
 */
public class Sys {

    private static final boolean DEBUG = true;

    public static void out(String s){
        if(DEBUG){
            System.out.println(s);
        }
    }

    public static String getXmlFileName(String className){
        if(className.endsWith(".java")){
            className = className.replaceAll(".java","");
        }
        if(className.endsWith(".JAVA")){
            className = className.replaceAll(".JAVA","");
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className) + ".xml";
    }
}
