package areahint.chat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ClientChatCompat {
    private static final List<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

    private ClientChatCompat() {
    }

    public static void register(Consumer<String> listener) {
        LISTENERS.add(listener);
    }

    public static void dispatch(String message) {
        for (Consumer<String> listener : LISTENERS) {
            listener.accept(message);
        }
    }
}
