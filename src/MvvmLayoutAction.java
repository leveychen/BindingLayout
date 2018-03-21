import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import utils.Sys;

/**
 * Created by levey on 2018/3/21.
 * Main Action
 */
public class MvvmLayoutAction extends BaseGenerateAction {

    @SuppressWarnings("unused")
    public MvvmLayoutAction() {
        super(null);
    }

    @SuppressWarnings("unused")
    public MvvmLayoutAction(CodeInsightActionHandler handler) {
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
        MvvmLayout ml = application.getComponent(MvvmLayout.class);
        Project project = null;
        Editor editor = null;
        try {
            project = event.getData(PlatformDataKeys.PROJECT);
            editor = event.getData(PlatformDataKeys.EDITOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(project == null || editor == null){
            ml.error("Error!");
            return;
        }

        PsiFile mFile = null;
        try {
            mFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mFile == null){
            ml.error("Error!");
            return;
        }
        PsiClass psiClass = null;
        try {
            psiClass = getTargetClass(editor, mFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(psiClass == null){
            ml.error("Error!");
            return;
        }
        if(mFile.getParent() !=null){
            Sys.out(Sys.getXmlFileName(mFile.getName()));
        }

        for (int i = 0; i < psiClass.getFields().length; i++) {
            Sys.out(psiClass.getFields()[i].getName());
        }

        if(project.getBasePath() != null){
            Sys.out(project.getBasePath());
        }

    }
}
