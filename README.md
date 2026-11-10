# Vaadin Tutorial Application

This repository holds the application that you build in the [Vaadin tutorial](https://vaadin.com/docs/latest/getting-started/tutorial): the product catalog of the back office of a fictional Click & Collect service. Users can list, sort, filter, view, edit, add, and delete products, and share links to individual products. The catalog runs inside a main layout with a navigation menu, next to a placeholder view for orders.

The `main` branch holds the finished application, as it is at the end of the last step of the tutorial. You can run it and read its code before you follow the tutorial. Every step also has a [branch of its own](#step-branches), so that you can start the tutorial from any step, or compare your code with the code at the end of a step.

## Run the Application

You need a Java Development Kit (JDK), version 21 or newer. Start the application with:

```bash
./mvnw
```

On Windows, run `mvnw` instead. The application opens at http://localhost:8080. Its database lives in memory, so your changes disappear when you restart the application, and the sample data comes back.

To build the application for production and run its tests, run:

```bash
./mvnw verify
```

## What's Inside

The application is a Spring Boot application, with an in-memory H2 database and Spring Data JDBC.

- [`com.example.product`](src/main/java/com/example/product) holds the product catalog: the view, its form and dialog, the application service, and the repositories. The classes are package-private, since nothing outside the package uses them.
- [`com.example.base.ui`](src/main/java/com/example/base/ui) holds the main layout and the title bar that the views share.
- [`com.example.order`](src/main/java/com/example/order) holds the placeholder view for orders.
- [`src/test/java/com/example/product`](src/test/java/com/example/product) holds a test of the service, and browserless tests of the view.
- [`schema.sql`](src/main/resources/schema.sql) and [`data.sql`](src/main/resources/data.sql) create the database and its sample data when the application starts.

## Step Branches

Each of these branches holds the application as it is at the end of a step of the tutorial:

| Step                                                                                                 | Branch                                                                              |
|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| 1. [Set Up Project](https://vaadin.com/docs/latest/getting-started/tutorial/setup)                   | [`step/setup`](https://github.com/vaadin/tutorial/tree/step/setup)                   |
| 2. [List Data](https://vaadin.com/docs/latest/getting-started/tutorial/list-data)                    | [`step/list-data`](https://github.com/vaadin/tutorial/tree/step/list-data)           |
| 3. [Sort & Filter](https://vaadin.com/docs/latest/getting-started/tutorial/sort-filter)              | [`step/sort-filter`](https://github.com/vaadin/tutorial/tree/step/sort-filter)       |
| 4. [Show Details](https://vaadin.com/docs/latest/getting-started/tutorial/show-details)              | [`step/show-details`](https://github.com/vaadin/tutorial/tree/step/show-details)     |
| 5. [Edit Details](https://vaadin.com/docs/latest/getting-started/tutorial/edit-details)              | [`step/edit-details`](https://github.com/vaadin/tutorial/tree/step/edit-details)     |
| 6. [Add Data](https://vaadin.com/docs/latest/getting-started/tutorial/add-data)                      | [`step/add-data`](https://github.com/vaadin/tutorial/tree/step/add-data)             |
| 7. [Add Navigation](https://vaadin.com/docs/latest/getting-started/tutorial/add-navigation)          | [`step/add-navigation`](https://github.com/vaadin/tutorial/tree/step/add-navigation) |
| 8. [Deep Link](https://vaadin.com/docs/latest/getting-started/tutorial/deep-link)                    | [`step/deep-link`](https://github.com/vaadin/tutorial/tree/step/deep-link)           |
| 9. [Add Tests](https://vaadin.com/docs/latest/getting-started/tutorial/add-tests)                    | [`step/add-tests`](https://github.com/vaadin/tutorial/tree/step/add-tests)           |

To start the tutorial at a step, clone the branch of the step before it. For example, to start at Edit Details, run:

```bash
git clone --branch step/show-details https://github.com/vaadin/tutorial.git
```

Each step is one commit on top of the previous step, so the [history of `step/add-tests`](https://github.com/vaadin/tutorial/commits/step/add-tests) lists the steps of the tutorial, and each commit shows what its step changes. In a clone, `git show origin/step/edit-details` shows the changes of the Edit Details step.

## Maintenance

Nobody edits the step branches by hand. [`GenerateSteps.java`](.github/steps/GenerateSteps.java) generates them from the tutorial in [vaadin/docs](https://github.com/vaadin/docs/tree/main/articles/getting-started/tutorial). It downloads the project skeleton that the Set Up Project step links to, adds the dependencies and SQL files that the step lists, and then applies the listings in the "Complete Code for This Step" section of each step, one step at a time.

The [CI workflow](.github/workflows/ci.yml) runs on every push to `main`, on every pull request, and every Monday. It:

- builds `main` and runs its tests,
- generates the steps from the branch of vaadin/docs in `DOCS_REF`, and builds and tests each of them with `./mvnw verify`,
- checks that `main` matches the last step, except for this README and the `.github` directory, and that this README lists every step branch, and
- on `main`, commits each step that changed on top of the previous step, and force-pushes its branch, and the branches of the steps after it.

To fix the code, fix the tutorial in vaadin/docs. The next CI run on `main` updates the step branches. If the last step changed, the check of `main` fails until you update `main` to match. To do that, generate the steps from the branch in `DOCS_REF`, here `v25.3`:

```bash
git clone --depth 1 --branch v25.3 --filter=blob:none --sparse https://github.com/vaadin/docs.git ../docs
git -C ../docs sparse-checkout set --no-cone /articles/_vaadin-version.adoc /articles/getting-started/tutorial/ /src/main/resources/META-INF/resources/vaadin/tutorial/
java .github/steps/GenerateSteps.java ../docs ../steps
```

Then copy the files from `../steps/add-tests` over the files of this repository, except for `README.md`, and commit them. Add them with `git -c core.autocrlf=false add`, so that `mvnw.cmd` keeps the Windows line endings that it has in the project skeleton.

When a new Vaadin version becomes the one at https://vaadin.com/docs/latest, set `DOCS_REF` in the CI workflow to the branch of vaadin/docs for that version, such as `v25.4`, and update `main` the same way.

## Other Versions

- The [`v24.4`](https://github.com/vaadin/tutorial/tree/v24.4) tag holds the Vaadin 24.4 tutorial application, a chat application built with both Flow and Hilla.
- The [`master`](https://github.com/vaadin/tutorial/tree/master) branch holds the Vaadin 8 tutorial application.
