package id.neotica.holomarket.feature.auth.common.ui;

import android.graphics.drawable.Drawable;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Toggles an {@link EditText}'s password mask when its right compound drawable (the "eye") is tapped.
 * <p>
 * The input type is left as {@code textPassword} — only the transformation method is swapped — so the
 * password IME is kept and the cursor/selection is preserved. The whole eye gesture is consumed, so the
 * field never starts its own text-selection gesture (which previously caused the entire text to be selected).
 */
public final class PasswordEyeToggle implements View.OnTouchListener {

    private final EditText editText;
    private final int eyeOpenRes;
    private final int eyeClosedRes;
    private boolean downOnEye;

    private PasswordEyeToggle(EditText editText, int eyeOpenRes, int eyeClosedRes) {
        this.editText = editText;
        this.eyeOpenRes = eyeOpenRes;
        this.eyeClosedRes = eyeClosedRes;
    }

    public static void attach(EditText editText, int eyeOpenRes, int eyeClosedRes) {
        editText.setOnTouchListener(new PasswordEyeToggle(editText, eyeOpenRes, eyeClosedRes));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable drawableRight = editText.getCompoundDrawables()[2];
        if (drawableRight == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downOnEye = isOnEye(drawableRight, event);
                return downOnEye;
            case MotionEvent.ACTION_UP:
                if (downOnEye) {
                    downOnEye = false;
                    toggle();
                    return true;
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
                downOnEye = false;
                return false;
            default:
                // Consume MOVE while the eye gesture is active so the field doesn't drag-select.
                return downOnEye;
        }
    }

    private boolean isOnEye(Drawable drawableRight, MotionEvent event) {
        return event.getX() >= (editText.getWidth()
                - editText.getPaddingRight()
                - drawableRight.getBounds().width());
    }

    private void toggle() {
        int cursor = editText.getSelectionStart();
        boolean masked = editText.getTransformationMethod() instanceof PasswordTransformationMethod;

        if (masked) {
            editText.setTransformationMethod(null);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, eyeOpenRes, 0);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, eyeClosedRes, 0);
        }

        int length = editText.getText().length();
        editText.setSelection(cursor < 0 ? length : Math.min(cursor, length));
    }
}