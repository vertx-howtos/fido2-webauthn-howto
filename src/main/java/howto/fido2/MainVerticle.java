package howto.fido2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.webauthn.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> start) {

    // tag::database[]
    // Dummy database, real world workloads
    // use a persistent store or course!
    InMemoryStore database = new InMemoryStore();
    // end::database[]

    // tag::setup[]
    // create the webauthn security object
    WebAuthn webAuthN = WebAuthn.create(
      vertx,
      new WebAuthnOptions()   // <1>
        .setRelyingParty(new RelyingParty()
          .setName("Vert.x FIDO2/webauthn Howto"))
        .setUserVerification(UserVerification.DISCOURAGED)  // <2>
        .setAttestation(Attestation.NONE)   // <3>
        .setRequireResidentKey(false)   // <4>
        .setChallengeLength(64)   // <5>
        .addPubKeyCredParam(PublicKeyCredential.ES256)    // <6>
        .addPubKeyCredParam(PublicKeyCredential.RS256)
        .addTransport(AuthenticatorTransport.USB)   // <7>
        .addTransport(AuthenticatorTransport.NFC)
        .addTransport(AuthenticatorTransport.BLE)
        .addTransport(AuthenticatorTransport.INTERNAL))
      // where to load/update authenticators data
      .authenticatorFetcher(database::fetcher)
      .authenticatorUpdater(database::updater);
    // end::setup[]

    // tag::routerInit[]
    final Router app = Router.router(vertx);
    app.route()   // <1>
      .handler(StaticHandler.create());
    app.post()    // <2>
      .handler(BodyHandler.create());
    app.route()   // <3>
      .handler(SessionHandler
        .create(LocalSessionStore.create(vertx)));

    WebAuthnHandler webAuthnHandler = WebAuthnHandler.create(webAuthN) // <4>
      .setOrigin(String.format("https://%s.nip.io:8443", System.getenv("IP")))
      // required callback
      .setupCallback(app.post("/webauthn/callback"))
      // optional register callback
      .setupCredentialsCreateCallback(app.post("/webauthn/register"))
      // optional login callback
      .setupCredentialsGetCallback(app.post("/webauthn/login"));

    app.route()
      .handler(webAuthnHandler);

    app.route("/protected")   // <5>
      .handler(ctx ->
        ctx.response()
          .end(
            "FIDO2 is Awesome!\n" +
              "No Password phishing here!\n"));
    // end::routerInit[]

    // tag::https[]
    vertx.createHttpServer(
      new HttpServerOptions()
        .setSsl(true)
        .setKeyStoreOptions(
          new JksOptions()
            .setPath("certstore.jks")
            .setPassword(System.getenv("CERTSTORE_SECRET"))))
      .requestHandler(app)
      .listen(8443, "0.0.0.0")
      .onSuccess(v -> {
        System.out.printf("Server: https://%s.nip.io:8443%n", System.getenv("IP"));
        start.complete();
      })
      .onFailure(start::fail);
    // end::https[]
  }
}
