package areahint.replacebutton;

import org.lwjgl.glfw.GLFW;

public final class ReplaceButtonKeyRules {
    private ReplaceButtonKeyRules() {
    }

    public static boolean isInvalidKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ESCAPE ||
            keyCode == GLFW.GLFW_KEY_ENTER ||
            keyCode == GLFW.GLFW_KEY_TAB ||
            keyCode == GLFW.GLFW_KEY_LEFT_SHIFT ||
            keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT ||
            keyCode == GLFW.GLFW_KEY_LEFT_CONTROL ||
            keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL ||
            keyCode == GLFW.GLFW_KEY_LEFT_ALT ||
            keyCode == GLFW.GLFW_KEY_RIGHT_ALT ||
            keyCode == GLFW.GLFW_KEY_LEFT_SUPER ||
            keyCode == GLFW.GLFW_KEY_RIGHT_SUPER ||
            keyCode == GLFW.GLFW_KEY_UNKNOWN;
    }
}
