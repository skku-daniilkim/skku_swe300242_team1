import com.skkuse.team1.socialhub.Config;
import com.skkuse.team1.socialhub.StarterVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRoutesTest {

    @BeforeAll
    public static void init(Vertx vertx, VertxTestContext tctx){
        // Deploy the server:
        vertx.deployVerticle(StarterVerticle.class.getName()).onComplete(tctx.succeedingThenComplete());
    }

    @Test
    @Order(0)
    public void testUser1(Vertx vertx, VertxTestContext tctx){
        // Create ConfigRetriever to extract ENV variables:
        ConfigRetriever.create(vertx).getConfig().compose(
                (configRetriever) -> WebClient.create(vertx)
                .get(   // Connect WebClient to the correct "IP:PORT":
                        configRetriever.getInteger(Config.KEY_PORT, Config.DEFAULT_PORT),
                        configRetriever.getString(Config.KEY_HOST, Config.DEFAULT_HOST),
                        // Set the API endpoint we are trying to access:
                        "/api/public/v1/test/user")
                .send()
        ).onComplete(tctx.succeeding((response) -> tctx.verify(() -> {
            // Check the result:
            // In this case, simply check if the returned status code is 200 OK:
            Assertions.assertEquals(200, response.statusCode());
            // Complete the test:
            tctx.completeNow();
        })));

    }

    @AfterAll
    public static void destroy(Vertx vertx, VertxTestContext tctx){
        // Cleanup test:
        tctx.completeNow();
        vertx.close();
    }

}

