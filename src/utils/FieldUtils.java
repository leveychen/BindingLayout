package utils;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by levey on 2018/3/22.
 * FieldUtils
 */
public class FieldUtils {
    /**
     * field has getter or not
     *
     * @return {@code true} field has getter
     * {@code false} otherwise
     */
    public static boolean hasGetter(@NotNull PsiField psiField) {
        return PropertyUtil.findGetterForField(psiField) != null;
    }

    /**
     * field has setter or not
     *
     * @return {@code true} field has setter
     * {@code false} otherwise
     */
    public static boolean hasSetter(@NotNull PsiField psiField) {
        return PropertyUtil.findSetterForField(psiField) != null;
    }

    /**
     * field has data binding getter or not
     *
     * @return {@code true} field has data binding getter
     * {@code false} field does not have getter or getter is Java Only
     */
    public static boolean hasDBGetter(@NotNull PsiField psiField) {
        PsiMethod getter = PropertyUtil.findGetterForField(psiField);
        return getter != null && getter.getModifierList().findAnnotation("android.databinding.Bindable") != null;
    }

    /**
     * field has data binding setter or not
     *
     * @return {@code true} field has data binding setter
     * {@code false} field does not have setter or setter is Java Only
     * ** only support this plugin format ,PR welcome **
     */
    public static boolean hasDBSetter(@NotNull PsiField psiField) {
        PsiMethod setter = PropertyUtil.findSetterForField(psiField);
        if (setter == null) {
            return false;
        }

        PsiCodeBlock codeBlock = setter.getBody();
        if (codeBlock == null) {
            return false;
        }
        for (PsiStatement psiStatement : codeBlock.getStatements()) {
            if (psiStatement.getText().toLowerCase().contains("notifyPropertyChanged".toLowerCase())) {
                return true;
            }
        }

        return false;
    }

}
