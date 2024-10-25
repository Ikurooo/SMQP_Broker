# Assignment 2: DSLab SMQP Server and DNS Server

This README serves as description for the project repository, examples of protocol responses and some information about
testing.
Please read the assignment description to find more details about the servers you have to implement and further
instructions.

## GitHub Classroom Grading

To grade this assignment, we will use GitHub Actions to automatically build and test the code submitted by students.
After pushing your
solution to the GitHub repository, the GitHub Actions will run automatically and provide feedback on the correctness of
the solution.
The grading feedback is provided at the end of the execution of the GitHub Actions Workflow. The feedback will contain
information about
each test case and whether it passed successfully or failed. If a test case failed, you will be able to see the error
output in the corresponding
execution step.

### Protect Files and Directories

The following files and directories are protected and should not be modified by students since this will flag the
submission with a `warning`:

- `.github/**/*` which is used for the GitHub Actions Workflows (e.g. Classroom Grading Workflow)
- `src/main/resources/**/*` which contains all the configuration files for this assignment
- `src/main/test/**/*` which contains all the tests and further helper classes for the test suite of this assignment
- `pom.xml` which defines all necessary external dependencies for this assignment and is used to build and test the
  project with GitHub Actions

### Assignment Structure

The structure of the assignment is as follows:

- `src/main/java/**/*` contains all the source code for this assignment
- `src/main/test/**/*` contains all the tests and further helper classes for the test suite of this assignment

# Protocols

The following responses and behaviors may not be exhaustive in regard to the assignment, and should only serve as a
base for your implementation.
If you come across situations not addressed below, feel free to create a solution that best fits the circumstances.

## Simple Message Queuing Protocol (SMQP)

Upon connecting to a Message Broker, the broker sends the greeting `ok SMQP`.

### `exchange <type> <name>`

Creates a new exchange with a given `name`, of a given `type`. Exchange types can be `fanout`, `direct`, `topic`.

#### Responses

| State                               | Response                                            |
|-------------------------------------|-----------------------------------------------------|
| success                             | `ok`                                                |
| error syntax                        | `error usage: exchange <type> <name>`               |
| exchange exists with different type | `error exchange already exists with different type` |

### `queue <name>`

Creates a new queue with a given `name`.

#### Responses

| State                      | Response                     |
|----------------------------|------------------------------|
| success                    | `ok`                         |
| error syntax               | `error usage: queue <name>`  |
| error no exchange declared | `error no exchange declared` |

### `bind <binding-key>`

Binds the previously defined exchange & queue to each other via a given `binding-key`.

#### Responses

| State                      | Response                          |
|----------------------------|-----------------------------------|
| success                    | `ok`                              |
| error syntax               | `error usage: bind <binding-key>` |
| error no exchange declared | `error no exchange declared`      |
| error no queue declared    | `error no queue declared`         |

### `publish <routing-key> <message>`

Publishes a given `message` to all queues where the `binding-key` matches the given `routing-key`.

#### Responses

| State                      | Response                          |
|----------------------------|-----------------------------------|
| success                    | `ok`                              |
| error syntax               | `publish <routing-key> <message>` |
| error no exchange declared | `error no exchange declared`      |

### `subscribe`

Subscribe to the previously defined queue. On successful execution all messages of the
corresponding queue will be forwarded to the client subscribed by calling this command.

#### Responses

| State                   | Response                  |
|-------------------------|---------------------------|
| success                 | `ok`                      |
| error no queue declared | `error no queue declared` |

### `exit`

Closes the connection.

#### Responses

- success: `ok`
- on failure: `error ...`
- on exit: `ok bye`

### Default Response

If no matching command of the protocol is found, then the broker sends `error protocol error` and closes the connection.

## Simple DNS Protocol (SDP)

Upon connecting to a DNS Server, the server sends the greeting `ok SDP`.

### `register <name> <ip:port>`

Registers a new domain.

#### Responses

| State        | Response                                 |
|--------------|------------------------------------------|
| success      | `ok`                                     |
| error syntax | `error usage: register <name> <ip:port>` |

### `resolve <name>`

Resolves the given domain `name` and responds with the associated `ip:port` combo.

#### Responses

| State                  | Response                      |
|------------------------|-------------------------------|
| success                | associated ip:port            |
| error syntax           | `error usage: resolve <name>` |
| error domain not found | `error domain not found`      |

### `unregister <name>`

Unregister a domain with the given name `name`.

#### Responses

| State        | Response                         |
|--------------|----------------------------------|
| success      | `ok`                             |
| error syntax | `error usage: unregister <name>` |

# Testing

This paragraph describes the default testing configuration for the available server (message-broker, dns-server).

## Naming scheme

The names of the servers start with `0` and are incremented by `1` for every additional server node.

- Message Broker: `broker-#`
- DNS Server: `dns-#`

For example the available servers for an integration test-case could look like this:

- `dns-0`
- `broker-0`
- `broker-1`
- `broker-2`
- `broker-3`

### Message Broker

The `broker-0` listens for publisher/subscribers on TCP port `20000` . The ports are incremented by 10 for every new broker instance.
So for example for the first three brokers we would have:

- `broker-0`: messaging=`20000`
- `broker-1`: messaging=`20010`
- `broker-2`: messaging=`20020`

### DNS Server

The `dns-0` listens per default on TCP port `18000`.

# Local Testing

You can test the client application locally by using the following commands:

```bash
# To execute all tests you can use the following command:
mvn test
# To execute a single test method you can use the following command:
mvn test -Dtest="<testClassName>#<testMethodName>"
# E.g., to execute the test method broker_shutdown_successfully from the class BasicBrokerTest you can use the following command:
mvn test -Dtest="BasicBrokerTest#broker_shutdown_successfully"
```

# Starting the Server Applications

To start the Message Broker or DNS Server application, you can use the following command:

```bash
# First compile the project with Maven
mvn compile
# Start the client application with the following command where componentId is one of client-0, client-1 or client-2.
mvn exec:java@<componentId>
# You can also combine both commands into one
mvn compile exec:java@<componentId>
```

You need to replace `<componentId>` with the corresponding component ID of the server: `broker-0`, `broker-1`,
`broker-2` and `dns-0` are available.
You may need multiple terminal windows to start multiple servers.

# Using netcat for Manual Testing

You can use netcat (nc) to connect to the server and send commands manually. To start a netcat client that connects to
the server at `localhost` on port `20000`, you can use the following commands:

```bash
# For Linux and macOS
# Open the terminal. The following command starts a netcat client that connects to the server at localhost on port 20000.
nc localhost 20000
```

```bash
# For Windows first ensure that you have installed netcat (ncat). If not, you can download it from the following link: https://nmap.org/download.html#windows
# Open CMD or PowerShell. The following command starts a netcat client that connects to the server at localhost on port 20000.
ncat -C localhost 20000
```
