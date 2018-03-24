package utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by levey on 2018/3/22.
 * WriterUtil
 * forked from https://github.com/Qixingchen/DataBindingModelFormatter/blob/master/src/moe/xing/databindingformatter/WriterUtil.java
 */

public class WriterUtil extends WriteCommandAction.Simple  {
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private Project mProject;
    private PsiFile mFile;
    private PsiField[] mFields;
    private boolean needAnnotation = false;


    public WriterUtil(PsiFile mFile, Project project, PsiClass mClass, PsiField[] fields) {
        super(project, mFile);
        mFactory = JavaPsiFacade.getElementFactory(project);
        this.mFile = mFile;
        this.mProject = project;
        this.mClass = mClass;
        this.needAnnotation = checkNeedAnn();
        this.mFields = fields;
    }

    private boolean checkNeedAnn(){
        for (int i = 0; i < mClass.getExtendsListTypes().length; i++) {
            if(mClass.getExtendsListTypes()[i].getClassName().contains(Sys.BASE_OBSERVABLE)){
                needAnnotation = true;
                return true;
            }
        }
        return false;
    }


    @Override
    protected void run() throws Throwable {
        addDataBinding();
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        CodeStyleManager.getInstance(mProject).reformat(mClass);
    }

    private void addDataBinding() {
        addConstructor();
        addGetterAndSetter(mFields);
    }


    private static final String CONSTRUCTOR_THIS = "this.";
    private static final String PUBLIC_MODIFIER = "public";
    private static final String FINAL_MODIFIER = "final";
    private static final String PRESENT_BOOLEAN = "boolean";
    private static final String CONSTRUCTOR_NAME = "CONSTRUCTOR_NAME";
    private static final String PROPERTY_CHANGE_REGISTRY = "PropertyChangeRegistry";

    private void addConstructor(){

        for (int i = 0; i < mClass.getConstructors().length; i++) {
            if(mClass.getConstructors()[i].getName().equals(mClass.getName())){
                return;
            }
        }

        PsiMethod emptyConstructor = mFactory.createConstructor();
        mClass.add(emptyConstructor);
        PsiMethod constructor = mFactory.createConstructor();
        constructor.setName(CONSTRUCTOR_NAME);
        constructor.getModifierList().setModifierProperty(PUBLIC_MODIFIER, true);
        Map<PsiField, PsiParameter> fields = new HashMap<>();
        for (PsiField field : mClass.getAllFields()) {
            if(field .getName() != null){
                if(field.getModifierList() != null && field.getModifierList().hasModifierProperty(FINAL_MODIFIER)){
                    continue;
                }
                if(field.getType().getPresentableText().equals(PROPERTY_CHANGE_REGISTRY)){
                    continue;
                }

                PsiParameter parameter = mFactory.createParameter(field.getName(), field.getType());
                constructor.getParameterList().add(parameter);
                fields.put(field, parameter);
            }

        }
        for (Map.Entry<PsiField, PsiParameter> entry : fields.entrySet()) {
            if(entry.getKey().getName() != null) {
                if (entry.getKey().getModifierList() != null && entry.getKey().getModifierList().hasModifierProperty(FINAL_MODIFIER)) {
                    continue;
                }

                if (entry.getKey().getType().getPresentableText().equals(PROPERTY_CHANGE_REGISTRY)) {
                    continue;
                }
                String en = CONSTRUCTOR_THIS + entry.getKey().getName() + " = " + entry.getValue().getName() + ";\n";
                PsiStatement c = mFactory.createStatementFromText(en, null);
                if (constructor.getBody() != null) {
                    constructor.getBody().add(c);
                }
            }
        }
        mClass.add(constructor);
    }


    /**
     * add getter and setter for fields
     *
     * @param fields filed need add getter / setter
     */
    private void addGetterAndSetter(PsiField[] fields) {
        String BRName = findBR();
        for (PsiField field : fields) {

            if(field.getType().getPresentableText().equals(PROPERTY_CHANGE_REGISTRY)){
                continue;
            }

            if(field.getModifierList() != null && field.getModifierList().hasModifierProperty(FINAL_MODIFIER)){
                continue;
            }

            boolean isBool = false;
            if(field.getType().getPresentableText().equals(PRESENT_BOOLEAN)){
                isBool = true;
            }


            if (!FieldUtils.hasDBGetter(field)) {
                if (FieldUtils.hasGetter(field)) {
                    addDBForJavaGetter(field);
                } else {

                    addObservableGetter(field,isBool);
                }
            }
            if (!FieldUtils.hasDBSetter(field)) {
                if (FieldUtils.hasSetter(field)) {
                    addDBForJavaSetter(field, BRName);
                } else {
                    addObservableSetter(field, BRName,isBool);
                }
            }
        }
    }

    /**
     * add DB getter for Field which does not have getter
     *
     * @param field field need add DB getter
     */
    private void addObservableGetter(@NotNull PsiField field,boolean isBoolean) {
        String getter =
                "public " + field.getType().getPresentableText() + (isBoolean ? " is" + isMethodHasIs(field) : " get"+ getFirstUpCaseName(field.getName())) +
                        "(){ \n" +
                        "return " + field.getName() + ";\n" +
                        "}";
        PsiMethod getMethod = mFactory.createMethodFromText(getter, mClass);
        if(needAnnotation) {
            getMethod.getModifierList().addAnnotation(Sys.BINDABLE);
        }
        mClass.add(getMethod);
    }

    /**
     * add DB part for a java getter
     *
     * @param psiField field which has a getter need add DB part
     */
    private void addDBForJavaGetter(@NotNull PsiField psiField) {
        PsiMethod getter = PropertyUtil.findGetterForField(psiField);
        assert getter != null;
        if(needAnnotation) {
            getter.getModifierList().addAnnotation(Sys.BINDABLE);
        }
    }

    /**
     * add DB setter for Field which does not have setter
     *
     * @param field field need add DB setter
     */
    private void addObservableSetter(@NotNull PsiField field, @NotNull String BRName,boolean isBool) {
        String setter = "public void set" + isMethodHasIs(field) +
                "(" + field.getType().getPresentableText() + " " +
                isSetterBrHasIs(field) + "){\n " +
                "        this." + field.getName() + " = " + isSetterBrHasIs(field) + ";\n" +
//                (needAnnotation ? "        notifyPropertyChanged( " + BRName + "." + (isBool ? isSetterBrHasIs(field) : field.getName()) + ");\n" : "" ) +
                (needAnnotation ? "        notifyPropertyChanged( " + BRName + "." + isSetterBrHasIs(field) + ");\n" : "" ) +
                "    }";
        mClass.add(mFactory.createMethodFromText(setter, mClass));
    }

    private String isSetterBrHasIs(@NotNull PsiField field){
        return getFirstLowCaseName(getFirstLowCaseName(field.getName()).replaceFirst("is",""));
    }

    private String isMethodHasIs(@NotNull PsiField field){
        return getFirstUpCaseName(getFirstLowCaseName(field.getName()).replaceFirst("is",""));
    }





    /**
     * add DB part for a java setter
     *
     * @param psiField field which has a setter need add DB part
     */
    private void addDBForJavaSetter(@NotNull PsiField psiField, @NotNull String BRName) {
        PsiMethod setter = PropertyUtil.findSetterForField(psiField);
        PsiCodeBlock codeBlock = setter.getBody();
        if (codeBlock == null) {
            return;
        }
        if(needAnnotation) {
            PsiStatement last = codeBlock.getStatements()[codeBlock.getStatements().length - 1];
            PsiStatement notify = mFactory.createStatementFromText("notifyPropertyChanged( " + BRName + "." + isSetterBrHasIs(psiField) + ");", setter);
            codeBlock.addAfter(notify, last);
        }
    }

    private String getFirstLowCaseName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private String getFirstUpCaseName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * find BR class name
     * find start with class package,to parent package
     * if not find ,return "BR"
     *
     * @return BR class name
     */
    @NotNull
    private String findBR() {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(getProject());
        String packageName = ((PsiJavaFile) mClass.getContainingFile()).getPackageName();
        while (packageName.contains(".")) {
            PsiClass BRClass = JavaPsiFacade.getInstance(getProject()).findClass(packageName + ".BR", scope);
            if (BRClass != null) {
                String name = BRClass.getQualifiedName();
                if (name != null && name.startsWith(".")) {
                    name = name.replaceFirst(".", "");
                }
                return name == null ? "BR" : name;
            }
            packageName = packageName.substring(0, packageName.lastIndexOf("."));
        }
        return "BR";
    }
}
