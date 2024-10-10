# BleatKan
A Veadotube WebSocket API Library written in Kotlin

Originally Created alongside the Touch Portal Veadotube Plugin, referencing the original Bleat Can C# Library

---

## Releases
The latest version is 0.6.0 and currently a beta release.  


- Very much a Work in Progress - stable enough for general use but hasn't been fully tested.
- Only Tested with Veadotube Mini v2.0
- Future Releases and the release of Veadotube (e.g. Full) will likely require updates, if not breaking changes.
- The Major Version will be bumped for any breaking changes



## Goals
- Provide an easy-to-use Library for working with Veadotube
- Be usable with Java as well as Kotlin
- Be Self-Contained, minimise the need to import Libraries this one depends on
- Use Concurrency to avoid bottlenecks
- Based on Bleat Can but is not intended to be an exact reimplementation



## Built using

- Kotlin 1.9/2.0
- Coroutines Library for concurrency
- Ktor & CIO for Web/WebSocket Client Library
- Kotlinx JSON Serialization Library for Serializing and Deserializing WebSocket Messages
- Log4J, SLF4J, and Kotlin Logging
- JUnit for Testing

---

## Using this Library

The Touch Portal Veadotube Plugin source code is the best example of use

**Main Classes**
- Instances Manager
  - Monitors .veadotube folder inside the user home
  - Decodes instance files and creates corresponding Objects
  - Sends Events/Objects to an Instances Listener
- Connection Class and Serializable Message Classes
  - Connects to Veadotube
  - Object Factory/Builder and Convenience Functions for common API Messages
  - Serializes and Deserializes Messaged to/from objects to avoid exposing libraries or creating compatibility issues
  - Message Objects are sent to a Connection Listener


**Generally you want to do the following:**
- Create a Class that implements Instances Listener and Connection Listener to Handle events
  - Can be Different Classes, but the Veadotube plugin implements both in the same class
- Create an Instances Manager, passing the Instances Listener
  - Create a Connection when it receives a Start event using the Instance Object it Receives
  - Clean up the Connection on End
    - Connection will have ended but Close() should be run and object dereferenced for collection
  - On Change should clean up the Old Connection and Create a new one
    - Veadotube ends connections if the Windows Title changes
    - Change event passes the old and new Instance Object
    - Close() should be run on the Connection and object dereferenced for collection
- For each Connection, pass the Connection Listener
  - Code to Register a Listener, Request Avatar List and other info should be done on Change when Active is true
  - Cleanup can be done on Change when Active is false
  - Error events can be ignored - Connection will retry connecting several times before giving up
  - Receive events contain Replies and Listener Events 
    - These are Deserialized Messages in the form of data Class Objects 
- Use VtRequest to create Requests and use the Connection Send function to send them

### Packages

#### Instance Package

**Instances Manager**
- Imports Instance Files and Manages Object Lifecycle
- Monitors `<user folder>/.veadotube/instances/` for Instance files  created by Veadotube Instances
- Sends Events to a Listener when Files Are Created, Modified, Deleted/Expired
  - Modified event only triggered if Instance Name Changes, internal file timestamps don't trigger modified event

**InstanceID - Represents an imported Instance File**
- Immutable - values can't be modified after creation
- Contains the values of the File Name, not file contents
  - File name is in format `<Instance Type>-<Launch Timestamp Hex>-<Process ID Hex>` e.g. *mini-08dc8d3c583c0587-00000f38*
  - These are separated to:
    - type (e.g. mini)
    - timestamp (as Long)
    - process (as Int)

**Instance - Represents the Server details of a single running copy of Veadotube**
- When created by Instance Manager, it contains the *Contents* of the Instance File
- Has the following values:
  - id - InstanceID the instance was imported from
  - server - WebSocket Server IP & Port. e.g. '127.0.0.1:23456'
  - name - Instance Display Name. e.g. 'veadotube mini'
  - fileLastModified - Internal Timestamp used to expire old files
  - instanceConnectionID - Unique ID Generated from ID, Server, Name

**Instances Listener**
- Interface for receiving events from Instances Manager


#### Connection Package

**Connection - A single Connection to a Veadotube Instance**
- Connects to an Instance of Veadotube using WebSocket
- Constructor needs an Instance and a Connection Listener
- Connection Listener is sent events and processed messages
- Messages mainly use the Serializable Package
  - Messages can be created with VtFactor, & sent using the Connection Send Function
- Received messages are processed and sent to Listener

**Connection Error**
- Errors that can be sent to the Listener

**Connection Listener**
- Listener for receiving events from Connection Manager


#### Message Package

**Request Message**
- VtFactory can be used to Generate Messages to send.
  - Has lots of convenience functions to make generation easy
- RequestMessage is the base Class for messages - different message types inherit this
- Several Enums for known values (message types, etc.) 

**Result Message**
- ResultMessage is the base Class for messages - different message types inherit this
- Several Enums for known values (message types, etc.)

**VtInstance**
- Contents of an Instance File

**Serializers**
- Serializer Objects for converting between Objects & JSON

---

## Links
- Veadotube: https://veado.tube
- VeadotubeTouchPortalPlugin : https://github.com/DissonantAU/VeadotubeTouchPortalPlugin
- Bleat Can (API): https://gitlab.com/veadotube/bleatcan
- Veadotube WebSocket Reference: https://veado.tube/help/docs/websocket
