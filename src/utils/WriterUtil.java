package utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by levey on 2018/3/22.
 * WriterUtil
 * forked from https://github.com/Qixingchen/DataBindingModelFormatter/blob/master/src/moe/xing/databindingformatter/WriterUtil.java
 */
public class WriterUtil extends WriteCommandAction.Simple {

    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private Project mProject;
    private PsiFile mFile;
    private PsiField[] mFields;

    public WriterUtil(PsiFile mFile, Project project, PsiClass mClass, PsiField[] fields) {
        super(project, mFile);
        mFactory = JavaPsiFacade.getElementFactory(project);
        this.mFile = mFile;
        this.mProject = project;
        this.mClass = mClass;
        this.mFields = fields;
    }

    @Override
    protected void run() throws Throwable {
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        addDataBinding();
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        CodeStyleManager.getInstance(mProject).reformat(mClass);
    }

    private void addDataBinding() {
        addGetterAndSetter(mFields);
    }


    /**
     * add getter and setter for fields
     *
     * @param fields filed need add getter / setter
     */
    private void addGetterAndSetter(PsiField[] fields) {
        for (PsiField field : fields) {

            Sys.out("PSI = " + field.getNameIdentifier().toString());
            if(field.getModifierList() != null){
                if(field.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)){
                    Sys.out("PSI PUBLIC = " + field.getNameIdentifier().toString());
                    continue;
                }
            }

            if (!FieldUtils.hasDBGetter(field)) {
//                if (FieldUtils.hasGetter(field)) {
//                    addDBForJavaGetter(field);
//                } else {
//                    addGetter(field);
//                }
                if (!FieldUtils.hasGetter(field)) {
                    addGetter(field,field.getType().equalsToText("boolean"));
                }
            }
            if (!FieldUtils.hasDBSetter(field)) {
//                if (FieldUtils.hasSetter(field)) {
//                    addDBForJavaSetter(field, BRName);
//                } else {
//                    addSetter(field, BRName);
//                }

                if (!FieldUtils.hasSetter(field)) {
                    addSetter(field);
                }
            }
        }
    }

    /**
     * add DB getter for Field which does not have getter
     *
     * @param field field need add DB getter
     */
    private void addGetter(@NotNull PsiField field,boolean isBoolean) {
        String getter =
                "public " + field.getType().getPresentableText() + (isBoolean ? " is" : " get") + getFirstUpCaseName(field.getName()) +
                        "(){ \n" +
                        "return " + field.getName() + "; \n" +
                        "}";
        PsiMethod getMethod = mFactory.createMethodFromText(getter, mClass);
        mClass.add(getMethod);
    }


    /**
     * add DB setter for Field which does not have setter
     *
     * @param field field need add DB setter
     */
    private void addSetter(@NotNull PsiField field) {
        String setter = "public void set" + getFirstUpCaseName(field.getName()) +
                "(" + field.getType().getPresentableText() + " " +
                field.getName() + "){\n " +
                "        this." + field.getName() + " = " + field.getName() + ";\n" +
                "    }";
        mClass.add(mFactory.createMethodFromText(setter, mClass));
    }


    private String getFirstUpCaseName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}