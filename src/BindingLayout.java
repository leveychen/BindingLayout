import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Created by levey on 2018/3/22.
 * Main ApplicationComponent
 */
public class BindingLayout implements ApplicationComponent {
    public BindingLayout() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "BindingLayout";
    }


    public void error(String s) {
        // Show dialog with message
        Messages.showMessageDialog(s, "Binding Layout", Messages.getErrorIcon());

    }

    public void info(String s) {
        // Show dialog with message
        Messages.showMessageDialog(s, "Binding Layout", Messages.getInformationIcon());

    }
}
