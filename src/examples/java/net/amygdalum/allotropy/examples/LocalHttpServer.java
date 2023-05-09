package net.amygdalum.allotropy.examples;

import static net.amygdalum.allotropy.ByName.byName;
import static net.amygdalum.allotropy.Scope.GLOBAL;

import java.nio.file.Paths;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import net.amygdalum.allotropy.Registrations;
import net.amygdalum.allotropy.URLProvider;
import net.amygdalum.allotropy.extensions.BeforeViewCallback;

public class LocalHttpServer implements BeforeViewCallback {

    @Override
    public void beforeView(Class<?> viewClass, Registrations registrations) {
        registrations.forScope(GLOBAL).resolve(byName("server"), Server.class, c -> Server.start());
    }

    public static record Server(Undertow server, int port) implements AutoCloseable, URLProvider {

        public static Server start() {
            int port = 8080;
            while (port <= 9000) {
                try {
                    Undertow server = Undertow.builder()
                        .addHttpListener(port, "localhost")
                        .setHandler(new ResourceHandler(new FileResourceManager(Paths.get("src/examples/resources/html").toFile())))
                        .build();
                    server.start();
                    return new Server(server, port);
                } catch (Exception e) {
                    port++;
                    continue;
                }
            }
            throw new IllegalStateException("cannot find appropriate port to start local http server");
        }

        @Override
        public String url(String path) {
            return "http://localhost:" + port + path;
        }

        @Override
        public void close() {
            server.stop();
        }
    }

}
