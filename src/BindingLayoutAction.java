
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
import com.intellij.psi.*;

import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;

import utils.Sys;
import utils.WriterUtil;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by levey on 2018/3/21.
 * Main Action
 */
public class BindingLayoutAction extends BaseGenerateAction {

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
            ml.error("Error!");
            return;
        }

        String moudleName = module.getModuleScope().getDisplayName().replaceAll("Module '", "").replaceAll("'", "");

        if (TextUtils.isEmpty(moudleName)) {
            ml.error("moudle is null");
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
            ml.error("package is null");
            return;
        }

        if (mFile.getParent() == null || project.getBasePath() == null) {
            ml.error("base path is null");
            return;
        }

        try {
            new WriterUtil(mFile, project, psiClass, psiClass.getFields()).execute();
        } catch (Throwable throwable) {
            ml.error("write getter and setter error");
            return;
        }

        String xmlFileName = Sys.getXmlFileName(mFile.getName());
        String xmlPath = project.getBasePath() + "/" + moudleName + "/src/main/res/layout/" + xmlFileName + "_view.xml";
        File xmlFile = new File(xmlPath);
        File xmlParent = xmlFile.getParentFile();
        if (!xmlParent.exists()) {
            xmlParent.mkdirs();
        } else if (xmlFile.exists()) {
            xmlFile.delete();
        }

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("layout");
        root.addAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
        Element data = root.addElement("data");
        Element variable = data.addElement("variable");
        variable.addAttribute("name", xmlFileName);
        variable.addAttribute("type", pkgName + "." + className);
        root.addComment(" add layout here ");
        OutputFormat format = new OutputFormat();
        format.setEncoding("utf-8");// 设置XML文件的编码格式
        format.setIndent(true);
        format.setIndentSize(4);
        format.setNewlines(true);
        XMLWriter write;
        try {
            write = new XMLWriter(new FileWriter(xmlFile), format);
            write.write(doc);
            write.close();
            VirtualFile vf = VfsUtil.findFileByIoFile(xmlFile, true);
            if (vf == null) return; // file not found
            new OpenFileDescriptor(project, vf).navigate(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
