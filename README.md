		API Spring Rest sécurisée à l'aide OAuth2 OpenId Connect et de KeyCloak 
		
		
# Aperçu (Overview): 

Ce tutoriel explique comment nous pouvons sécuriser nos API REST à l'aide du protocole OpenId Connect. Nous utiliserons KeyCloak comme fournisseur d'identité (IAM).      
# OAuth (Open Authorization):
est un protocole standard d'autorisation **déléguée**. Il permet aux applications d'accéder aux données de l'utilisateur sans le mot de passe de l'utilisateur.      
L'*autorisation déléguée* est une approche permettant à une application tierce d'accéder aux données d'un utilisateur.      
### Terminologie OAuth 2.0:  
Comprendre ce protocole nous oblige à comprendre sa terminologie:     
##### Les acteurs concernés:    
- Propriétaire de la ressource (ou utilisateur) : l'utilisateur propriétaire des données auxquelles l'application cliente souhaite accéder.     
- Client (Application client): l'application qui souhaite accéder aux données de l'utilisateur.    
- Serveur d'autorisation : est le composant qui effectue l'authentification et l'autorisation, il gère les demandes de connexion, l'authentification des utilisateurs, la génération de jetons et les validations de sécurité.     
- Serveur de ressources: le système qui contient les données auxquelles le client souhaite accéder. Dans certains cas, le serveur de ressources et le serveur d'autorisation sont identiques. L'une des différences entre le serveur d'autorisation et le serveur de ressources est que le premier gère "uniquement" l'authentification et l'autorisation, et le second ne sert "que" le contenu (les ressources).        

##### Les flux OAUTH (Understanding Different OAuth Flows):     
OAUTH 2.0 expose **4** flux différents, Il n'est pas obligatoire de les implémenter tous, mais seulement ceux dont vous avez besoin.     
Le but reste toujours le même, est d'obtenir un **access_token (jeton d'accès)** et l'utiliser pour accéder aux ressources protégées.       
- Accorder le code d'Autorisation (Authorization Code Grant): un code est émis et utilisé pour obtenir le access_token . Ce code est publié dans une application frontale (sur le navigateur) après la connexion de l'utilisateur. Le jeton d'accès à la place est émis côté serveur, authentifiant le client avec son mot de passe et le code obtenu .    
- Accord implicite (Implicit Grant):  une fois que l'utilisateur s'est connecté, le jeton d'accès est émis immédiatement.   
- Octroi d'informations d'identification du client (Client Credential Grant): le jeton d'accès (access_token) est émis sur le serveur, authentifiant uniquement le client, pas l'utilisateur.     
- Accord mot de passe (Password Grant): le jeton d'accès est émis immédiatement avec une seule demande contenant toutes les informations de connexion : nom d'utilisateur, mot de passe utilisateur, identifiant client et secret client. Cela pourrait sembler plus facile à mettre en œuvre, mais cela comporte quelques complications.      

##### Configuration OAuth 2.0:   
Lorsque la demande d'accord d'autorisation est lancée, le client envoie certaines données de configuration au serveur d'autorisation en tant que paramètres de requête **query params**. Les paramètres de requête de base sont:    

- client_id: cet ID aide le serveur d'autorisation à déterminer le client qui lance le flux OAuth.   
- redirect_uri: l'URL à laquelle le serveur d'autorisation enverra (via une redirection) le code d'autorisation , après la connexion de l'utilisateur.    
- response_type: le type de réponse que nous voulons obtenir du serveur d'autorisation.   
- scope: une liste de permis que l'application demande à l'utilisateur. Par exemple : read_email , write_post . L'utilisateur sera invité à accorder ces autorisations. Cela sera utile lorsque le client accédera au serveur de ressources.   
- client_secret: est fourni par le service d'autorisation. Ce paramètre peut être obligatoire ou non, en fonction du flux OAuth.   
  
### Un exemple du monde réel:    
En clair, OAuth 2.0 permet à vos clients de créer un compte sur votre application web en se connectant sur un compte appartenant à une société vérifiée, comme Google, Facebook, Twitter,.. ou un serveur de gestion des identités et des accès comme de gestion des identités et des accès.     
Cette application web vous permet de créer un compte avec vos identifiants Google ou Facebook. Beaucoup d’utilisateurs choisissent ce mode de connexion, facile et rapide. Dans ce cas, Google et Facebook sont qualifiés de fournisseurs d’identité (IdP). Ces derniers sont utilisés pour authentifier un utilisateur et gérer son identité.     

Décrivons le Code Workflow de l’autorisation de OAuth 2.0 (flux numéro 1): 
Dans cet exemple on va se connecter à notre application Web OpenClassRoom avec mon compte Facebook.    
- Étape 1 - Connection au serveur d’autorisation de Facebook: Il existe deux acteurs dans cette étape, l'utilisateur, c’est vous, la personne qui se connecte à OpenClassrooms avec OAuth2, et le client, qui est l’application web à laquelle vous vous connectez avec le bouton “Se connecter avec Facebook”.          
On click sur le bouton connecter à Facebook, Le client vous dirige vers le serveur d’autorisation de Facebook.      
Il s’agit du serveur auquel vous vous connectez grâce à vos identifiants Facebook. Ce serveur est enregistré avec OpenClassrooms.        
- Étape 2 - Autorisation avec OAuth: Lorsque que vous entrez vos identifiants Facebook, l’indication suivante apparaît, “Autorisez-vous OpenClassrooms à accéder à votre liste de contacts ?”, OAuth s’assure alors que vous acceptez ce périmètre (scope, en anglais).   
Si vous répondez oui, vous serez redirigé vers l’URL de redirection avec un code d’autorisation. Il s’agit d’un code temporaire qui détient les informations concernant vos identifiants Facebook.      
- Étape 3 - Autorisation en échange d’un token d’accès: Le client envoie le code d’autorisation au serveur d’autorisation de Facebook, en échange d’un token d’accès à votre liste de contacts. Ce token détient des informations d’autorisation de Facebook.   
- Étape 4 - Validation avec le serveur de ressources: Ce token est envoyé vers le serveur API de Facebook, qui est appelé serveur de ressources dans ce workflow. Ce serveur vérifie les informations sur le token d’accès, et permet au client d’accéder aux données d’utilisateur requises dans l’application. 

![Alt text](https://github.com/zyedtu/secure-spring-rest-api-using-oauth2-openid-connect-and-keycloak/blob/master/src/main/resources/flux-code-autorisation.png?raw=true "Title")

# OpenID Connect (OIDC):   
OpenID Connect est une couche d'identité au-dessus du protocole OAuth 2.0. Il étend OAuth 2.0 pour normaliser un moyen d'authentification.       
- OAuth 2.0: sert pour l'autorisation.     
- OpenID Connect:  sert à l'authentification.    

Si vous êtes confu	s par ces termes, voici la différence entre eux.

- l'authentification: celui qui utilise l’application doit être identifié par un couple username/password.
- l'autorisations: tous les utilisateurs n’ont pas nécessairement accès aux mêmes fonctionnalités. Par exemple, un utilisateur non administrateur ne doit pas pouvoir modifier de compte autre que le sien.    

![Alt text](https://github.com/zyedtu/secure-spring-rest-api-using-oauth2-openid-connect-and-keycloak/blob/master/src/main/resources/openIDConnect.png?raw=true "Title")

OAuth ne fournit pas immédiatement l'identité de l'utilisateur, mais plutôt un jeton d'accès pour l'autorisation. OpenID Connect permet au client d'identifier l'utilisateur sur la base de l'authentification effectuée par le serveur d'autorisation. Ceci est réalisé en définissant un périmètre-une portée (**scope**) nommée *openid* lors de la demande au serveur d'autorisation pour la connexion et le consentement de l'utilisateur. openid est une portée- scope obligatoire pour indiquer au serveur d'autorisation qu'OpenID Connect est requis.       
Le résultat de la requête est un code d'application que le client peut échanger contre un jeton d'accès et un jeton d'identification.     
Le jeton d'identification est un jeton Web JSON ou JWT. Un JWT est un jeton codé composé de trois parties : en-tête, charge utile (payload) et signature.    
Après avoir acquis le jeton d'identification, le client peut le décoder pour obtenir les informations de l'utilisateur encodées dans la partie charge utile **payload** — comme ceci:  

	{ 
	  "iss": "https://accounts.google.com", 
	  "sub": "10965150351106250715113082368", 
	  "email": "johndoe@example.com", 
	  "iat": 1516239022, 
	  "exp": 1516242922 
	}
Le Payload du jeton d'identification contient certains champs appelés **claims ou clè**. Les claims (les clés) de base sont:  

- iss: émetteur de jetons.    
- sub: identifiant unique de l'utilisateur.    
- email: e-mail de l'utilisateur.   
- iat: heure d'émission du jeton représentée en heure Unix.   
- exp: heure d'expiration du jeton représentée en temps Unix.   

Si le client a besoin de plus d'informations sur l'utilisateur, il peut spécifier des scope OpenID Connect standard pour indiquer au serveur d'autorisation d'inclure les informations requises dans la charge utile du jeton d'ID. Ces scopes sont { profile, email, address, phone }.       
# keycloak:   
Keycloak est une solution open source de gestion des identités et des accès (**IAM Identity and Access Management**) destinée aux applications et services modernes. Keycloak fournit des services d'authentification et d'autorisation prêts à l'emploi ainsi que des fonctionnalités avancées telles que la fédération d'utilisateurs, le courtage d'identité et la connexion sociale.   
### Installation de keyCloak:
L'installation avec docker-compose n'est pas voulu aboutir, du coup j'ai installé aver la commande run sur une image.  

	> docker run -p 9080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:18.0.0 start-dev

Cela démarrera Keycloak exposé sur le port local 9080. Il créera également un utilisateur administrateur initial avec un nom d'utilisateur admin et un mot de passe admin.      
Ensuite pour accéder à l'interface graphique on tappe cet url:

	http://localhost:9080/admin
### Configurer KeyCloak pour sécuriser une API Rest:  
KeyCloak est livré avec un domaine par défaut de Master.     
On va créer un nouveau domain, on click sur **Add realm**, on suite on ajoute un domain qu'on l'appelle Microservices.     
### Enregistrez notre API Rest en tant que client KeyCloak OAuth 2.0:   
Sélectionnez Clients dans le menu de navigation de gauche, puis cliquez sur le bouton **Créer**. Veuillez confirmer que vous êtes dans le domaine MicroServices:    
TODO part-1
# Mettre en place une API Rest:   
Dans cette partie on va mettre une application web restful seécuriéé par spring sécurité. Cette application va gérer les Student Service, est un simple CRUD API pour créer et supprimer des étudiants.   
###  Configuration de Spring Security:   
La classe *WebSecurityConfig* configure Spring Security au niveau de la requête Web.    

	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
		@Override
	    protected void configure(HttpSecurity http) throws Exception {
	        http.cors()
	                .and()
	                .authorizeRequests()
	                .anyRequest()
	                .authenticated()
	                .and()
	                .oauth2ResourceServer()
	                .jwt();
	    }
	}

Comme on travaille avec OAUTH 2.0 on a besoin d'ajouter cette deépendence dans notre pom.xml

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
		</dependency>

Nous aurons besoin de spring-boot-starter-oauth2-resource-server , le démarreur de Spring Boot pour la prise en charge du serveur de ressources. Ce démarreur inclut Spring Security par défaut, nous n'avons donc pas besoin de l'ajouter explicitement.        

https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-1
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-2
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-3
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-4
https://ravthiru.medium.com/springboot-oauth2-with-keycloak-for-bearer-client-3a31f608a78
https://www.baeldung.com/spring-security-oauth-resource-server