I want you to create a proof of concept in this project to demonstrate how to implement somecustom security logic. I want you to create two springboot 4 Java 25 with maven apps and one react js app with vite.
The first springboot app should be called "supervisor" and the second "messages". The React app should be called frontend.

Both the sprinngboot apps should implement security using JWT tokens. The supervisor can be the authorization server and also a resource server, while the messages app can be a resource server that relies on the supervisor for authentication and authorization. The messages app should accept JWT tokens issued by the supervisor and validate them before allowing access to its endpoints, and it should also accept jwt tokens that were created using a shared secret key.

The frontend app should have a simple login form that allows users to authenticate with the supervisor app and obtain a JWT token. The frontend should then use this token to access protected in both the supervisor and messages apps. The frontend should also have a logout functionality that invalidates the JWT token on the client side.

I know that usually when messages app will start the supervisor app should be running to validate so messages can retrieve the public key that will used to validate the tokens, but I want you to implement a fallback mechanism in the messages app that allows it to continue functioning even if the supervisor app is down. In this case, the messages app will reject any JWT tokens that were issued by the supervisor, but it will still accept and validate JWT tokens that were created using the shared secret key. This way, the messages app can continue to function and provide access to its endpoints even if the supervisor app is unavailable.

The UI should only be using JWT tokens issued by the supervisor app for authentication and authorization, and it should not be aware of the shared secret key mechanism used by the messages app as a fallback. The UI should also handle token expiration and refresh tokens if necessary to maintain a seamless user experience.

Also add a simple script that I can use to create the JWT token using the shared secret key for testing purposes.

Make a plan of the implementation steps for this proof of concept and write them down in a clear and organized manner in a markdown format.