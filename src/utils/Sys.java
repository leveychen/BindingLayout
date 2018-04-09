package utils;

import com.google.common.base.CaseFormat;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;

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



    public static final String BASE_SERIALIZABLE = "Serializable";
    public static final String IMPORT_SERIALIZABLE = "java.io.";
    public static final String BASE_OBSERVABLE = "BaseObservable";
    public static final String BINDABLE = "Bindable";
    public static final String IMPORT_BASE = "android.databinding.";

    public static ExtendsType hasExtends(PsiClass psiClass){
        for (int i = 0; i < psiClass.getExtendsListTypes().length; i++) {
            if(psiClass.getExtendsListTypes()[i].getClassName().contains(Sys.BASE_OBSERVABLE)){
                return ExtendsType.TYPE_BASE_OBSERVABLE;
            }
        }
        if(psiClass.getExtendsListTypes().length > 0){
            return ExtendsType.TYPE_OTHER;
        }
        return ExtendsType.TYPE_NONE;
    }

    public static boolean hasImplements(PsiClass psiClass){
        for(PsiClassType s : psiClass.getImplementsListTypes()){
            if(s.getClassName().contains(Sys.BASE_SERIALIZABLE)){
                return true;
            }
        }
        return false;
    }

    public enum ExtendsType{
        TYPE_NONE,
        TYPE_OTHER,
        TYPE_BASE_OBSERVABLE
    }

}
