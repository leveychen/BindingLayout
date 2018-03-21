import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Created by levey on 2018/3/21.
 * Main ApplicationComponent
 */
public class MvvmLayout implements ApplicationComponent {
    public MvvmLayout() {
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
        return "MvvmLayout";
    }


    public void error(String str) {

        // Show dialog with message

        Messages.showMessageDialog(

                str,

                "Mvvm Layout",

                Messages.getInformationIcon()

        );

    }
}
