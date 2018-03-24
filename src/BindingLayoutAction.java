
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import utils.Sys;
import utils.WriterUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by levey on 2018/3/22.
 * Main Action
 */
public class BindingLayoutAction extends BaseGenerateAction {

    private static GlobalSearchScope mSearchScope;

    @SuppressWarnings("unused")
    public BindingLayoutAction() {
        super(null);
    }

    @SuppressWarnings("unused")
    public BindingLayoutAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    protected boolean isValidForClass(final PsiClass targetClass) {
        return super.isValidForClass(targetClass);
    }

    @Override
    public boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return super.isValidForFile(project, editor, file);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        // TODO: insert action logic here
        Application application = ApplicationManager.getApplication();
        BindingLayout ml = application.getComponent(BindingLayout.class);
        DataContext dataContext = event.getDataContext();
        final Project project = DataKeys.PROJECT.getData(dataContext);
        final Module module = DataKeys.MODULE.getData(dataContext);
        final Editor editor = DataKeys.EDITOR.getData(dataContext);

        if (project == null || editor == null || module == null) {
            ml.error("system error!");
            return;
        }

        if(mSearchScope == null){
            mSearchScope = GlobalSearchScope.allScope(project);
        }



        PsiClass dataBinding = JavaPsiFacade.getInstance(project).findClass("android.databinding.Observable", mSearchScope);
        if (dataBinding == null) {
            ml.error("have you enabled data binding in build.gradle?\n\n" +
                    "android {\n" +
                    "    dataBinding {\n" +
                    "        enabled = true\n" +
                    "    }\n" +
                    "}");
            return;
        }
        

        String moudleName = module.getModuleScope().getDisplayName().replaceAll("Module '", "").replaceAll("'", "");

        if (TextUtils.isEmpty(moudleName)) {
            ml.error("moudle null");
            return;
        }

        PsiFile mFile = null;
        try {
            mFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mFile == null) {
            ml.error("system error");
            return;
        }

        String className = mFile.getName().replaceAll(".java", "").replaceAll(".JAVA", "");

        PsiClass psiClass = null;
        try {
            psiClass = getTargetClass(editor, mFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (psiClass == null) {
            ml.error("system error");
            return;
        }

        PsiJavaFile javaFile = (PsiJavaFile) mFile;
        String pkgName = javaFile.getPackageName();
        if (TextUtils.isEmpty(pkgName)) {
            ml.error("package null");
            return;
        }

        if (mFile.getParent() == null || project.getBasePath() == null) {
            ml.error("project path null");
            return;
        }

        if(psiClass.getFields().length <= 0){
            ml.error("empty entity fields");
            return;
        }

        try {
            new WriterUtil(mFile, project, psiClass, psiClass.getFields()).execute();
        } catch (Throwable throwable) {
            ml.error("IllegalArgumentException: please check entity fields");
            return;
        }


        String xmlFileName = Sys.getXmlFileName(mFile.getName());
        String xmlPath = project.getBasePath() + "/" + moudleName + "/src/main/res/layout/" + xmlFileName + "_view.xml";
        File xmlFile = new File(xmlPath);
        File xmlParent = xmlFile.getParentFile();
        if (!xmlParent.exists()) {
            xmlParent.mkdirs();
        } else if (xmlFile.exists()) {
           // xmlFile.delete();
            ml.info("layout already exists, reopen now");
            openXml(ml,project,xmlFile,xmlFileName);
            return;
        }


        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n");
        sb.append("<layout xmlns:android=\"http://schemas.android.com/apk/res/android\">\n");
        sb.append("    <data>\n");
        sb.append("        <import type=\"android.view.View\"/>\n");
        sb.append("        <variable\n");
        sb.append("            name=\"").append(Sys.getTypeName(mFile.getName())).append("\"\n");
        sb.append("            type=\"").append(pkgName).append(".").append(className).append("\"\n");
        sb.append("        />\n");
        sb.append("    </data>\n");
        sb.append("\n");
        sb.append("    <LinearLayout\n");
        sb.append("        android:layout_width=\"match_parent\"\n");
        sb.append("        android:layout_height=\"match_parent\"\n");
        sb.append("        android:orientation=\"vertical\">\n");
        sb.append("\n");
        sb.append("        <!-- add layout here -->\n");
        sb.append("\n");
        sb.append("    </LinearLayout>\n");
        sb.append("</layout>\n");
        try {
            FileWriter fw = new FileWriter(xmlFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();
            fw.close();
            openXml(ml,project,xmlFile,xmlFileName);
        } catch (Exception e) {
            ml.error("file being used :\n\n" + xmlPath);
        }


    }

    private void openXml(BindingLayout ml,Project project,File xmlFile,String xmlPath){
        VirtualFile vf = VfsUtil.findFileByIoFile(xmlFile, true);
        if (vf == null) {
            ml.error("file not found :\n\n" + xmlPath);
            return; // file not found
        }
        OpenFileDescriptor ofd = new OpenFileDescriptor(project, vf);
        if(ofd.canNavigate() && ofd.canNavigateToSource()){
            ofd.navigate(true);
        }else {
            ml.error("cannot open :\n\n" + xmlPath);
        }

    }

}
