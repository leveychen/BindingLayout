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
    private GlobalSearchScope mSearchScope;
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private Project mProject;
    private PsiJavaFile mFile;
    private PsiField[] mFields;
    private boolean needAnnotation = false;
    private String mPkgName;
    private boolean needImplements = false;


    public WriterUtil(GlobalSearchScope searchScope, String mPkgName, PsiJavaFile mFile, Project project, PsiClass mClass,
                      PsiField[] fields, Sys.ExtendsType extendsType,boolean hasImplements) {
        super(project, mFile);
        this.mFactory = JavaPsiFacade.getElementFactory(project);
        this.mSearchScope = searchScope;
        this.mFile = mFile;
        this.mProject = project;
        this.mClass = mClass;
        this.needAnnotation = extendsType == Sys.ExtendsType.TYPE_BASE_OBSERVABLE;
        this.mFields = fields;
        this.mPkgName = mPkgName;
        this.needImplements = !hasImplements;
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
        addImports();
        addExtends();
        addImplements();
        addConstructor();
        addGetterAndSetter(mFields);
    }


    private static final String CONSTRUCTOR_THIS = "this.";
    private static final String PUBLIC_MODIFIER = "public";
    private static final String FINAL_MODIFIER = "final";
    private static final String CONSTRUCTOR_NAME = "CONSTRUCTOR_NAME";
    private static final String PROPERTY_CHANGE_REGISTRY = "PropertyChangeRegistry";


    private void addImports(){
        if(needAnnotation){
            checkImports(Sys.IMPORT_BASE + Sys.BASE_OBSERVABLE);
            checkImports(Sys.IMPORT_BASE + Sys.BINDABLE);
            checkImports(mPkgName + ".BR");
        }
        if(needImplements){
            checkImports(Sys.IMPORT_SERIALIZABLE + Sys.BASE_SERIALIZABLE);
        }
    }

    private void checkImports(String importName){
        assert mFile.getImportList() != null;
        for (PsiImportStatement importStatement : mFile.getImportList().getImportStatements()){
            if(importStatement.getQualifiedName() != null && importStatement.getQualifiedName().equals(importName)) {
                return;
            }
        }
        PsiClass impClz = JavaPsiFacade.getInstance(getProject()).findClass(importName, mSearchScope);
        if (impClz != null) {
            PsiImportStatement imp = mFactory.createImportStatement(impClz);
            mFile.getImportList().add(imp);
        }

    }

    private void addImplements(){
        if(needImplements) {
            if (mClass.getImplementsList() != null) {
                PsiClassType extendsClassType = PsiClassType.getTypeByName(Sys.BASE_SERIALIZABLE, mProject, mSearchScope);
                mClass.getImplementsList().add(mFactory.createReferenceElementByType(extendsClassType));
            }
        }
    }

    private void addExtends(){
        if(needAnnotation) {
            if (mClass.getExtendsListTypes().length <= 0) {
                if (mClass.getExtendsList() != null) {
                    PsiClassType extendsClassType = PsiClassType.getTypeByName(Sys.BASE_OBSERVABLE, mProject, mSearchScope);
                    mClass.getExtendsList().add(mFactory.createReferenceElementByType(extendsClassType));
                }
            }
        }

    }

    private void addConstructor(){
        for (PsiMethod method : mClass.getConstructors()){
            method.delete();
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

//            boolean isBool = false;
//            if(field.getType().getPresentableText().equals(PRESENT_BOOLEAN)){
//                isBool = true;
//            }

            if (!FieldUtils.hasDBGetter(field)) {
                if (FieldUtils.hasGetter(field)) {
                    addDBForJavaGetter(field);
                } else {

                    addObservableGetter(field);
                }
            }
            if (!FieldUtils.hasDBSetter(field)) {
                if (FieldUtils.hasSetter(field)) {
                    addDBForJavaSetter(field, BRName);
                } else {
                    addObservableSetter(field, BRName);
                }
            }
        }
    }

    /**
     * add DB getter for Field which does not have getter
     *
     * @param field field need add DB getter
     */
    private void addObservableGetter(@NotNull PsiField field) {
        String getter =
                "public " + field.getType().getPresentableText() + " " + FieldUtils.getGetterName(field) +
                        "(){ \n" +
                        "return " + field.getName() + ";\n" +
                        "}";
        PsiMethod getMethod = mFactory.createMethodFromText(getter, mClass);
        addAnnotation(getMethod);
        mClass.add(getMethod);
    }

    private void addAnnotation(PsiMethod psiMethod){
        if(needAnnotation) {
            for (PsiAnnotation annotation : psiMethod.getModifierList().getAnnotations()) {
                if (annotation.getQualifiedName() != null && annotation.getQualifiedName().contains(Sys.BINDABLE)) {
                    return;
                }
            }
            psiMethod.getModifierList().addAnnotation(Sys.IMPORT_BASE + Sys.BINDABLE);
        }
    }

    /**
     * add DB part for a java getter
     *
     * @param psiField field which has a getter need add DB part
     */
    private void addDBForJavaGetter(@NotNull PsiField psiField) {
        PsiMethod getter = PropertyUtil.findGetterForField(psiField);
        assert getter != null;
        addAnnotation(getter);
    }

    /**
     * add DB setter for Field which does not have setter
     *
     * @param field field need add DB setter
     */
    private void addObservableSetter(@NotNull PsiField field, @NotNull String BRName) {
        String setter = "public void " + FieldUtils.getSetterName(field) +
                "(" + field.getType().getPresentableText() + " " +
                field.getName() + "){\n " +
                "        this." + field.getName() + " = " + field.getName() + ";\n" +
                (needAnnotation ? "        notifyPropertyChanged( " + BRName + "." + field.getName() + ");\n" : "" ) +
                "    }";
        mClass.add(mFactory.createMethodFromText(setter, mClass));
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
            PsiStatement notify = mFactory.createStatementFromText("notifyPropertyChanged( " + BRName + "." + FieldUtils.getSetterName(psiField) + ");", setter);
            codeBlock.addAfter(notify, last);
        }
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
        String packageName = ((PsiJavaFile) mClass.getContainingFile()).getPackageName();
        while (packageName.contains(".")) {
            PsiClass BRClass = JavaPsiFacade.getInstance(getProject()).findClass(packageName + ".BR", mSearchScope);
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
