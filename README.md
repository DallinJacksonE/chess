# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Dallin Jackson Notes

### Sequence Diagram (Phase 2)
[Link to Sequence Diagram of the API calls](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5MAek8B9P-4DAnwAlFHskVQs5JAg0IPi-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-VsS-ZOvfA21QQbw6YIVhuN2zQPL+plj7YAxpLBgJnWAocxgrauxtmul2C6oK+vic-Zlrr41YBroa9NF6p1NsiPc3uZ6OiinNSyiSPQlj8o2JtaQEk179QEpmGUAj3zVTNYsHQCBQCKUg2NaDf6UDek-NEmAowgW9PlSiP5BDFgodUN+39n4ANAZA2B3YEHtYbGITKWD8HEN0eQ5+NDGwMNYaoaYfFrC4DBFBAoZhpKKUsIJXS7AJtsDcHgL+Wol7DBssET84RXKHpDDGBMFDgrg3oGxCh9jKAAZhoZZK9RYIZVKY7TghVvQlV9pGTvcZ0lJm4ysai2xJMHHcecXXEJunWqdBlKFWoDatR0tM9Go+zcrO3rrlFnJ+a5MoAbbPK0SaX14TfaUoySBwgEHjGl911nV69GzACrejmB173LEVpN+qq3LJjnAZLMkc0oHKP6z8dXbQoLQZAFqPqmvczjncROKAt2ZtDQdSLw7Y2VwEamu95z67ZPm0l2yKXuuViTXMLZubmBluHpWyB2WvQJfWyFwwKG0uIMXtg0r3anC9pEiqkxYz1UH0HvN0dSzx1XYbcnNjn552uX9ku3cMlvJNTXduzBBxkXlAbbuvWLRx0wHmIVYKzw91o+tobBtAHj1g8vp7Qn0glt13J5l5e90yCpGCu8QysnNsKYQe8+7OjO2I3+cqlD5HujAdA5QvFi1AgACkBjbRE5SvaIqsgoDRJ8AAbMrmA4uICHjZ5kRjcKbpCM5e+h685NOeG04fMB2JpPAEsJQRFvMoAHH59IYzhd3iq7cG4MzVNNGxa5zZvD9nXv9o+4O1zWqPOzK879nDy20IY8OHB23UB7fQHOOClgm1mGeGNCtBQcAADSJDncUeF3ah5w3PYa8PA2ltYArdJ7txAB3TuyPSAOJm7DpzK-dFGwgDgoDMFw5DXSlXyvPerZjcfBbKa4tGsn9F4eAArTXaBUs7buVWs73RNq5fy3GMKPXn0Pb0WV57yqQ+Y0HbVjfkQY-d-Ryvmvn5yQDfQK3-97f83N-scT4KLRl0A0Nh1wgdP8Ud90T0CdPwEI-ZSdugG0EJYlHUYAn818dZ5d9R3dx8vdhgWI1hU9YBfRmAFA+9dAZM-c3400VtcDrwCCc080SD1QyCxAt8QUvRq9Lo9x1QTZDA20PkStT8nshIHNVVQ894aD8Cf9YBs1ZIWJ5IYAlIVIRc6wMCx8J9Us9xDIEBZU5971Us2AQJhgTgYA+hG8X575mI-wadq0ODtluCgx2d20T9bMhCL8qsxDpJUtZDWJFDWplDFk35mh0d5xsBxtzRUsYD8dwdl10hxtAdYiwoOAEDtAhxdCVtbDUt6dGdDJrDt9yAGdLImdtCMjus+DOccNudfledsRHEeMxc+NGEFBgh74+g+gaU6VkgbceoWJYBgBsBpNCBShLorMOUyoRFehxEmFQRnpjAIth4fdLNFsBCXCg9KtRCr8JlNV3MbEo82R788I49CJEAejpBjRrCe9ui8BTiht0djirizi-ZlZ81uA8ByRwCIdugQAXjeijJ95pgP8UADg39LpE5gBLY8cD0ZwsjLI-83Y30Y5oT8xKd59ET1Bci2DuhUTVAiijAUBuBUU54OdPkssA8Mx8FDEMcu8Di6cIZGJfjygoUYU4UZZv80BkUDkF4YBuNTAgA)
