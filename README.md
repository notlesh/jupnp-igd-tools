# jupnp-igd-tools

A sandbox for interacting with the [jupnp](https://github.com/jupnp/jupnp) library to request port forwards and query for the external IP address in a NAT environment.

### Building

Requirements:

* Java JDK / JRE version 10+
* Gradle
* jupnp pre-built (see below)

To build:
```
./gradlew build
```

#### jupnp bug

The current jupnp library has a bug which precludes using the official Maven repositories. See my [Pull Request](https://github.com/jupnp/jupnp/pull/117) for more details.

The current work around is to pull from [my jupnp fork](https://github.com/notlesh/jupnp) and build locally:

```
git clone https://github.com/notlesh/jupnp
cd jupnp
git checkout dev
mvn clean install
```

Then copy or symlink the following build artifacts into a `libs` directory in the `jupnp-igd-tools` repo:

```
cd <local jupnp-igd-tools dir>
mkdir libs
ln -s $JUPNP_DIR/bundles/org.jupnp.support/target/org.jupnp.support-2.6.0-SNAPSHOT.jar ./libs/
ln -s $JUPNP_DIR/bundles/org.jupnp/target/org.jupnp-2.6.0-SNAPSHOT.jar ./libs/
ln -s $JUPNP_DIR/bundles/org.jupnp.osgi/target/org.jupnp.osgi-2.6.0-SNAPSHOT.jar ./libs/
```

### Running

Using gradle to run is trivial using the `run` task and `--args=...` command to pass command line args. Currently supported:

* `./gradlew run --args="-h"` for help
* `./gradlew run --args="--query-external-ip"` to query for external IP address
* `./gradlew run --args="--map-port ..."` to request a port mapping
