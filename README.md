# Akk / Vue

Une petite application pour jouer avec Akka et VueJS

### Quelques prérequis
 * Une connaissance légère de SBT
 * Une connaissance légère du protocole HTTP

## Créer un projet Akka-HTTP
Créer un projet Akka-HTTP est très simple, il suffit d'ajouter une dépendence à son `build.sbt`:
```
"com.typesafe.akka" %% "akka-http" % "10.0.3"
```
En réalité, Akka-HTTP a besoin de deux modules, `akka-http` et `akka-http-core`.
Puisque le premier dépend du second, il n'est pas nécessaire de l'importer explicitement.

### Mettre en place un serveur
Akka-HTTP présente un DSL permettant de décrire des routes HTTP et la façon de les gérer.
Chaque route est composée d'un ou plusieurs niveau de `Directives` permettant de  spécifier la gestion d'un type spécifique de requête.

```
val route =
  path("hello") { // Route gérée
    get { // Directive particulière spécifiant ne gérer que les requêtes GET
      complete( // Réponse à envoyer pour une telle directive
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "<h1>Say hello to akka-http</h1>"
          )
        )
    }
  }
```

Les routes sont ensuite liées à un port pour démarrer un serveur HTTP:
```
Http().bindAndHandle(route, "localhost", 8080)
```

### Jouer avec les path
Il existe de nombreuses directives permettant divers comportement de gestion des paths.
 * [`path(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/path.html) match avec un slash (`/`) suivi par `x` puis rien d'autre.
 * [`pathPrefix(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/pathPrefix.html) matche avec un slash (`/`) suivi par `x` puis une suite qui reste non matchée.
 * [`pathEnd(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/pathEnd.html) matche uniquement lorsqu'il n'y a plus rien à matcher dans le path. Akka précise qu'il ne faut pas l'utilser à la racine des routes puisque le path minimal est `/`.

Exemple:
```
val route =
  pathPrefix("ball") {
    pathEnd {
      complete("/ball")
    } ~
      path(IntNumber) { int =>
        complete(if (int % 2 == 0) "even ball" else "odd ball")
      }
  }

// tests:
Get("/") ~> route ~> check {
  handled shouldEqual false
}

Get("/ball") ~> route ~> check {
  responseAs[String] shouldEqual "/ball"
}

Get("/ball/1337") ~> route ~> check {
  responseAs[String] shouldEqual "odd ball"
}
```

### La sérialisation
Dans Akka-HTTP, on appelle ça "Marshalling".
Le principe est de convertir une structure haut-niveau (un objet) en une représentation plus bas niveau.
Ici, on parle de la conversion d'un type `T` en un type plus bas niveau, par exemple le `MessageEntity` qui pourra être utilisé dans une `HttpRequest` ou une `HttpResponse`.

Pour marshaller une instance d'un type `A` en un type `B`, on utilise un `Marshaller[A, B]`.
`Marshaller[A, B]` n'est pas vraiment une fonction `A => B` mais plutôt un `Future[List[Marshalling[B]]]`.
 * `Future` car il est possible que les marshallers ne soient pas capables de produire un résultat de manière synchrone.
 * `List` puisque plutôt que d'offrir une unique représentation d'un type `A`, un marshaller peut en proposer plusieurs. C'est via l'addition d'un header `Accept` que le client pourra décider celui qui est préféré. Autrement, le premier sera choisi.
 * `Marshalling[B]` est utilisé plutôt que directement `B` pour préciser un `MediaType` et potentiellement un `HttpCharset` que le marshaller produira avant que le marshalling soit déclenché.
En plus de permettre une négociation de contenue (via l'utilisation de la liste), ce design permet de retarder la construction de l'instance cible du marshalling au dernier moment.

Il existe de [nombreux marshallers](http://doc.akka.io/docs/akka-http/current/scala/http/common/marshalling.html#predefined-marshallers) pré-définis dans Akka-HTTP.

#### Démarshaller
Le processus existe dans l'autre sens et est appelé "Unmarshalling".
Il s'agit donc de convertir une représentation bas niveau en une structure plus haut niveau.
La structure utilisée pour ce genre de conversion est un `Unmarshaller[A, B]`, qui est ici une structure très similaire à une fonction `A => Future[B]` puisqu'il n'y a pas d'intérêt à supporter une négociation de contenu et qui permet de gagner plusieurs niveaux d'indirections.

#### Le JSON
Le JSON n'est pas une des structure supportée nativement pas Akka-HTTP, il est nécessaire d'utiliser un module supplémentaire.
Celui conseillé est [spray-json](https://github.com/spray/spray-json) pour lequel un module dédié a été créé:
```
"com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3"
```
Avec ça en main, nous allons pouvoir créer des format json aisément:
```
path("foo") {
  import spray.json.DefaultJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  case class Foo(i: Int)
  implicit val fooFormat = jsonFormat1(Foo)
  complete(Foo(5))
}
```

### Utiliser Akka-HTTP comme un serveur haut niveau
Tout ce qu'on a vu jusque là permet surtout de gérer des requêtes HTTP basiques, sans envoi de fichiers ni possibilité de servir des  ressources serveur.
Avant de commencer à essayer de mettre en place la partie front de notre application, nous devons donc voir comment faire pour servir des fichiers HTTP, JS et CSS.
Il existe des directives permettant ce genre de manipulations, j'ai écrit cela:
```
get {
  pathPrefix("(.+/?)*".r) { asset => // Match tout chemin 'aa/bb/cc' possible
    encodeResponse { // optionally compresses the response with Gzip or Deflate if the client accepts compressed responses
      getFromFile(s"$workingDirectory/client/$asset") // serve up static content
    }
  }
}
```
Ce genre de directives permet de servir du contenu statique, comme une page HTML, du JS ou du CSS.


## Vue.js
Vue est un framework permettant de construire des interfaces utilisateur.

### Installation
```
npm install vue --save
```

Pour une utilisation avec webpack, il va falloir rajouter des alias.
Par défaut, les packages NPM n'exportent pas le build qu'il nous faut.
```
resolve: {
  alias: {
    vue: 'vue/dist/vue.js'
  }
}
```

### Utilisation
Vue.js permet d'afficher de la donnée dans le DOM de manière déclarative:
```
<div id="app">
  {{ message }}
</div>
```
et
```
var app = new Vue({
  el: '#app',
  data: {
    message: 'Hello Vue!'
  }
})
```
Ces deux bouts de code permettent de créer une application Vue.
La donnée et le DOM sont liés avec ce code, ce qui rend tout réactif: si `app.message` est modifié, le texte affiché dans le DOM l'est aussi.

#### Directives
Une directive est un attribut HTML particulier, préfixée avec `v-` (pour indiquer qu'il s'agit d'un attribut fournit par Vue.js).
Elles permettent d'appliquer un comportement spécial au DOM affiché.


## Akka Streams
On s'est bien amusés, mais j'aimerai pouvoir transmettre des données de façon streamées à mon front.
Pour se faire, il faut bien comprendre ce qu'il se passe derrière notre Akka-HTTP, et notamment qu'il se base sur une autre librairie d'Akka, [Stream](http://doc.akka.io/docs/akka/2.4/scala/stream/index.html).

### Des concepts de base
#### Stream
Il s'agit d'un processus actif qui transforme et déplace de la donnée.
#### Element
Il s'agit de l'unité de process d'un stream.
Toute opération d'un stream transforme et transfère des éléments d'un upstream à un downstream.
On exprime la taille des buffets en nombre d'éléments indépendamment de la taille de ces éléments.
#### Back-pressure
Il s'agit d'une mesure de contrôle, une façon pour les consommateurs de donnée de notifier un producteur de leur disponibilité courante, pour qu'il puisse ainsi ajuster sa vitesse de production en fonction.
Dans Akka Streams, la back-pressure est toujours comprise comme étant non-bloquante et asynchrone.
#### Non bloquant
Cela signifie qu'une opération ne gênera pas l'avancement du thread appelant, même si elle met du temps à se terminer.


### Un quick start guide
On retrouve trois grands types dans Akka Streams pour définir et lancer des streams:
 * `Source`: il s'agit d'un objet qui émet un type de valeurs.
 Cet objet est paramétré par deux types. Le premier est le type d'éléments émit par cette source, le second signal que lancer cette source produit une valeur auxiliaire.
 Créer une source signifie avoir une description de ce que l'on veut lancer mais pas qu'elle est active.
 Pour récupérer son contenu, il est nécessaire de la lancer, aucun calcul n'est fait auparavant.
 * `Sink`: il s'agit d'un objet qui reçoit un type de valeurs.
 C'est le genre d'objets que l'on utilise pour lancer une source et récupérer son contenu.
 * `Flow`: il s'agit d'un élément qui a un type de valeur en entrée et un en sortie.
 Il fait la connexion entre ces deux types et décrit les différentes étapes permettant de passer d'un type à l'autre.

On parle aussi d'un quatrième type, le RunnableGraph.
Il s'gait d'un `Flow` attaché à une `Source` et un `Sink` et qui est donc prêt à être lancé via `run()`.
