		API Spring Rest sécurisée à l'aide OAuth2 OpenId Connect et de KeyCloak 
		
		
# Aperçu (Overview):   
Ce tutoriel explique comment nous pouvons sécuriser nos API REST à l'aide du protocole OpenId Connect. Nous utiliserons KeyCloak comme fournisseur (gestion) d'identité et des accès (Identity and Access Management - IAM).       
# On va parler un peu de Single Sign-On (SSO) et contrôle d’accès:   
le SSO, est une approche nécessairement globale de **l’authentification unique**, est un procédé qui permet de garantir une authentification unique aux utilisateurs du système d’information pour accéder à leurs applications, et ainsi de faire disparaitre les très nombreux couples identifiant/mot de passe qu’ils utilisent au quotidien pour travailler.        

Fonctionnellement, le SSO s’appuie d’abord sur une première authentification réalisée par l’utilisateur : par exemple celle servant à se connecter à un poste de travail, ou à un portail d’entreprise. Ensuite, pendant toute la durée de sa session SSO, et pour toutes les applications auxquelles cet utilisateur souhaite accéder, le SSO prend en charge le processus d’authentification à ces applications à la place de l’utilisateur, et ce de manière transparente pour ce dernier.    

Le marché du Single Sign-On (« SSO » ou authentification unique) est traditionnellement divisé en **4 catégories de solutions**.     
### L’ENTERPRISE SSO: 
est généralement mis en œuvre en interne dans l’entreprise à des fins de confort utilisateur. Il nécessite le déploiement d’un (ou plusieurs) composant(s) sur les postes de travail reliés au système d’information et consiste à injecter – à la place des utilisateurs – des accréditations secondaires (couples identifiant/mot de passe des utilisateurs pour les applications visées) dans des fenêtres applicatives qui ont été au préalable enrôlées. L’avantage de cette catégorie de SSO est de pouvoir couvrir facilement tout type d’applications (client lourd, web, virtualisée, mainframe, etc.) ; l’inconvénient est qu’il faut maîtriser tous les postes de travail sur lesquels on veut déployer ce SSO.    
### Le Web SSO:  
 s’inscrit dans des architectures 100% web, de type portails extranet/intranet par exemple. Il ne couvre par conséquent que des applications web.   
### LA FÉDÉRATION D’IDENTITÉ:   
Dans une approche purement technique, la fédération d’identité peut être considérée comme un moyen d’opérer une action de Web SSO en utilisant les protocoles standards associés du marché : SAMLv2, OAuth2, OpenID Connect, WS-Federation.    
### LE MOBILE SSO:   
Le Mobile SSO permet de fournir des fonctionnalités de Single Sign-On (Enterprise SSO, Web SSO, Fédération d’identité) sur les périphériques mobiles (smartphones, tablettes), et ainsi de sécuriser les accès aux applications du système d’information depuis ces périphériques.    
# Quelques façons d'authentifier une requête HTTP:   
* Authentification de base HTTP: Il s'agit de la technique la plus simple dans laquelle nous combinons nom d'utilisateur et mot de passe pour former une seule valeur. Cette valeur unique est ensuite encodée avec Base64 et transmise via l'en-tête HTTP Authorization. Le serveur vérifie l'en-tête d'autorisation et le compare aux informations d'identification stockées (nom d'utilisateur et mot de passe). S'ils correspondent, le serveur répond à la demande du client. Cependant, s'ils ne correspondent pas, le code d'état HTTP 401 pour indiquer un accès non autorisé est renvoyé au demandeur. Ce code informe le client de l'échec de l'authentification et la demande du client est donc refusée. Regardons l'exemples suivant avec **httpBasic()**:   

			@Override
			protected void configure(HttpSecurity http) throws Exception {
				http
				.csrf().disable()
				.authorizeRequests().anyRequest().authenticated()
				.and()
				.httpBasic();
			}
    
* Authentification OAuth: OAuth est une authentification basée sur les **jetons** on va voir d'avantage sur ce protocole dans ce cours

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
OpenID Connect permet aux clients de vérifier l'identité de l'utilisateur final sur la base de l'authentification effectuée par un serveur d'autorisation, ainsi que d'obtenir des informations de profil de base sur l'utilisateur final d'une manière interopérable et de type REST.     
OpenID Connect s'appuie sur OAuth2 et ajoute une authentification. OpenID Connect ajoute des contraintes à OAuth2 comme le point de terminaison UserInfo (Endpoint), le jeton d'identification (JWT est le format obligatoire pour le jeton)...      
OpenID Connect utilise des jetons **JWT** pour authentifier les applications Web.     
• OAuth 2.0: sert pour l'autorisation.     
• OpenID Connect:  sert à l'authentification.    

Si vous êtes confus par ces termes, voici la différence entre eux.

• l'authentification: celui qui utilise l’application doit être identifié par un couple username/password.        
• l'autorisations: tous les utilisateurs n’ont pas nécessairement accès aux mêmes fonctionnalités. Par exemple, un utilisateur non administrateur ne doit pas pouvoir modifier de compte autre que le sien.    

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
Keycloak fournit une solution d'authentification unique (sso), ce qui signifie qu'un utilisateur peut s'authentifier sur plusieurs systèmes avec un seul identifiant de connexion et un seul mot de passe.   
### Installation de keyCloak:
##### With Docker cmd:

	> docker run -p 9080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:18.0.0 start-dev
Cela démarrera Keycloak exposé sur le port local 9080. Il créera également un utilisateur administrateur initial avec un nom d'utilisateur admin et un mot de passe admin.      
Ensuite pour accéder à l'interface graphique on tappe cet url:

	http://localhost:9080/admin
##### With docker compose:  
Dans mon projet j'ai opté avec *docker compose*, pour cela fonctionne j'ai du aussi ajouté une configuration dans *Dockerfile*:    
Tout d'abort je crée le fichier docker-cmpose.yml:   

		version: '3.8'
		
		services:
		    keycloak:
		        build:
		            context: ./keycloak
		        container_name: keycloak
		        hostname: keycloak
		        environment:
		            DB_VENDOR: h2
		            KEYCLOAK_LOGLEVEL: WARN
		            KEYCLOAK_ADMIN: admin
		            KEYCLOAK_ADMIN_PASSWORD: admin
		        ports:
		            - "8089:8080"
		            - "9990:9990"
Ensuite sous le repetoire keycloak je crée le fichier Dockerfile:    

	FROM quay.io/keycloak/keycloak:17.0.0
	RUN /opt/keycloak/bin/kc.sh build
	
	ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
et pour lance le docker compose je lance cette commande:

	> docker compose up -d 
la j'ai ajouté le -d pour le mode détach.      

Cela démarrera Keycloak exposé sur le port local 8089. Il créera également un utilisateur administrateur initial avec un nom d'utilisateur *admin* et un mot de passe *admin*.      
Ensuite je peux vérifier que tout va bien avec l'interface graphique sur cette url:     

	http://localhost:8089/
### Configurer KeyCloak pour sécuriser une API Rest:  
KeyCloak est livré avec un domaine par défaut de Master.     
On va créer un nouveau domain, on click sur **Add realm**, on suite on ajoute un domain qu'on l'appelle student-oidc (student Open ID Connect).           
### Enregistrez un client KeyCloak OAuth 2.0:   
Sélectionnez Clients dans le menu de navigation de gauche, puis cliquez sur le bouton **Créer**. Veuillez confirmer que vous êtes dans le domaine student-oidc:    
On crée le client *manager-student* avec c'est deux information:   
	- ID client: manager-student   
	- Protocole client : openid-connect   

KeyCloak présentera maintenant une fenêtre pour ajouter des propriétés supplémentaires pour ce client:   
	- Access Type: confidential     
	- Valid redirect URIs: http://localhost:3000/*    

Remarque : des URI de redirection valides sont nécessaires si vous essayez d'accéder à l'API via une application frontale comme Angular . Puisque nous allons accéder à l'API via Postman , cette URL peut être n'importe quelle URL et n'a pas besoin d'être une URL active.      
### Ajouter des rôles:
Dans l'onglet **Roles** on clique et on add les nouveaux rôles, dans mon cas j'ai ajouté deux rôles:   
	- ADMIN.    
	- USER.   
### Créer un ClientScope:
TODO     
### Ajouter des utilisateurs:
Dans l'onglet  **Users** on ajoute des nouveaux utilisateurs, add -> le nom de l'utilisateur ensuite save.   
Ensuite dans *Credentials* on crée le password de l'utilisateur.   
Enfin on donne le rôle de l'utilisateur dans *Role Mapping*.   
Dan mon projet j'ai crée deux utilisatuers.  
	- ziedadmin avec un rôle ADMIN (password admin)    
	- zieduser avec un rôle USER (password user)    
	
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
### The application.yml file:  
Dans notre fichier *application.yml* on va ajouter les détails du serveur KeyCloak, Spring Security utilise ces informations pour se connecter à KeyCloak et vérifier les informations d'identification de chaque utilisateur tentant d'accéder à l'API StudentService.  Cette URI est un format **standard**.       
spring:
  security: 
    oauth2: 
      resourceserver: 
        jwt:
          issuer-uri : http://localhost:$port/auth/realms/$realm     
$port: 	le port sur lequel le serveur Keycloak est exécuté, dan smon cas est 8089     
$realm: le nom du domaine configuré dans mon cas est student-oidc.   
La configuration complète est:  

		spring:
		  security:
		    oauth2:
		      resourceserver:
		        jwt:
		          issuer-uri : http://localhost:8089/realms/student-oidc

# Créer un code d'autorisation (Token) dans Postman:

### Récupérer le secret client:  
Cliquez sur l'onglet Informations d'identification et copiez le secret. Ce sera le secret client que nous utiliserons pour nous authentifier auprès de KeyCloak      

![Alt text](https://github.com/zyedtu/secure-spring-rest-api-using-oauth2-openid-connect-and-keycloak/blob/master/src/main/resources/credentials_client_oauth2.png?raw=true "Title")

### Récupérer l'URL de jeton:  
Nous devons également obtenir l' URL du jeton Keycloak pour générer un code d'autorisation (Token) pour notre client. Nous pouvons obtenir cela à partir du domaine *student-oidc* . Cliquez sur le paramètre Realm, puis cliquez sur Endpoints.   

![Alt text](https://github.com/zyedtu/secure-spring-rest-api-using-oauth2-openid-connect-and-keycloak/blob/master/src/main/resources/token_url.png?raw=true "Title")

L'URL récupérée est ci-dessous:

		http://localhost:8089/realms/student-oidc/protocol/openid-connect/token
Nous avons maitenant tous les paramètres  dont nous avons besoin pour nous authentifier avec KeyCloak   et créer un code d'autorisation:    
	- Client id.   
	- Client Secret.   
	- Keycloak Token Endpoint.   
	
Nous pouvons maintenant utiliser ces paramètres dans Postman pour créer un code d'autorisation (**token**). Ce token sera ensuite envoyé dans le header à chaque appel à l'API du service Étudiant. Nous devrions alors pouvoir invoquer toutes les opérations sans obtenir d'erreur d'autorisation.    
### Créer le token dans Postman:     

![Alt text](https://github.com/zyedtu/secure-spring-rest-api-using-oauth2-openid-connect-and-keycloak/blob/master/src/main/resources/postman_token.png?raw=true "Title")

# Ajout des règle d'autorisation: 
Dans cette partie on va créer une API qui permet d'ajouter un étudiant (student), mais que l'utilisateur qui a le rôle ADMIN peut faire cette action.    
### L'ajout du controller:   

	@PostMapping(value = "/v1/students")
	public ResponseEntity<Integer> saveStudent(@RequestBody Student student) {
		Integer studentId = studentService.integrationStudent(student);
		return new ResponseEntity<>(studentId, HttpStatus.CREATED);
	}
### On crée une Enum pour les rôles:  

		public enum Roles {
			ADMIN,
			USER
		}  
### Configuration spring sécurity:   

	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.cors()
				.and()
				.authorizeRequests()
					.antMatchers(HttpMethod.POST, "/v1/students")
					.hasRole(Roles.ADMIN.name())
				.anyRequest()
				.authenticated()
				.and()
				.oauth2ResourceServer()
				.jwt();
		}
	}

Avec **antMatchers**: on dit que pour la méthode POST et avec l'URI /v1/students, il y a que les utilisateurs qui ont le rôle **hasRole(Roles.ADMIN.name())** peuvent faire cette action.    








https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-1     
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-2    
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-3     
https://www.todaystechnology.org/post/secure-spring-rest-api-using-openid-connect-and-keycloak-part-4     
https://ravthiru.medium.com/springboot-oauth2-with-keycloak-for-bearer-client-3a31f608a78    
https://www.baeldung.com/spring-security-oauth-resource-server     
https://blog.devgenius.io/secure-your-spring-boot-application-using-keycloak-8c63e0530089     
