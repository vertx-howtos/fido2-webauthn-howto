const loginButton = document.getElementById('login');
const registerButton = document.getElementById('register');
const messageDiv = document.getElementById('message');

const displayMessage = message => {
  messageDiv.innerHTML = message;
}

// tag::create[]
const webAuthn = new WebAuthn({
  callbackPath: '/webauthn/callback',
  registerPath: '/webauthn/register',
  loginPath: '/webauthn/login'
});
// end::create[]

// tag::register[]
registerButton.onclick = () => {
  webAuthn
    .register({
      name: document.getElementById('username').value,
      displayName: document.getElementById('displayName').value
    })
    .then(() => {
      displayMessage('registration successful');
    })
    .catch(err => {
      displayMessage('registration failed');
      console.error(err);
    });
};
// end::register[]

// tag::login[]
loginButton.onclick = () => {
  webAuthn
    .login({
      name: document.getElementById('username').value
    })
    .then(() => {
      displayMessage('You are logged in');
    })
    .catch(err => {
      displayMessage('Invalid credential');
      console.error(err);
    });
};
// end::login[]
