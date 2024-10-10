package io.github.dissonantau.bleatkan.connection


import io.ktor.websocket.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import io.github.dissonantau.bleatkan.instance.Instance
import io.github.dissonantau.bleatkan.instance.InstanceID
import io.github.dissonantau.bleatkan.message.RequestMessage
import io.github.dissonantau.bleatkan.message.ResultMessage
import io.github.dissonantau.bleatkan.message.VtRequest
import io.github.dissonantau.bleatkan.message.ResultPayload
import kotlin.test.Test


class ConnectionTest {

    @Test
    fun testConnection() {
        assertNotNull(conn) { "Connection is Null, Should never be the case" }

        conn?.let { conn ->

            assert(conn.server == dummyInstance.server) { "Connection Server doesn't Match Dummy Instance Value" }

            assert(conn.name == dummyInstance.name) { "Connection Name doesn't Match Dummy Instance Value" }

            assert(
                conn.connUri == Instance.getWebSocketUri(
                    dummyInstance.server,
                    dummyInstance.name
                )
            ) { "Connection URI doesn't expected Instance" }
        }
    }

    /**
     * Test Incoming Message Processing
     *
     * This feeds a Frame into a Channel and checks the result
     *
     * Examples from Default Avatar `O Gato` by BELLA!
     */
    @Test
    fun testReceiveTypeList() = runTest {
        val channel = "nodes"

        /*
        * Type List Request Reply
        *
        * Request:
        * val requestTypeList = """nodes:{"event":"list"}"""
        */
        val testName = "Type List Request Reply"

        // Basic JSON
        val replyTypeList =
            """{"event":"list","entries":[{"type":"stateEvents","id":"mini","name":"avatar state"}]}"""

        // Create Decoded Generic JSON - this just verifies the JSON is Valid
        assertDoesNotThrow { Json.parseToJsonElement(replyTypeList) }

        // Create frame and 'Send'
        frameSender.send(Frame.Text("$channel:$replyTypeList"))

        // Create a Decoded message from JSON and add channel
        val jsonVtMessage: ResultMessage =
            Json.decodeFromString(replyTypeList)
        jsonVtMessage.channel = channel


        // Get decoded
        var lastReceive: Pair<Connection, ResultMessage>? = null
        while (lastReceive == null) {
            delay(1000)
            lastReceive = connectionListener.lastConnectionReceive
        }

        lastReceive.let { (_, resultMessage) ->
            println("Testing: $testName")

            assert(resultMessage is ResultMessage.ResultMessageWithEntryList)

            assert(resultMessage.event == "list")
            resultMessage as ResultMessage.ResultMessageWithEntryList

            for (entry in resultMessage.entries) {
                when (entry.name) {
                    "avatar state" -> {
                        assert(entry.type == "stateEvents" && entry.id == "mini")
                    }

                    else -> throw IllegalStateException("Unexpected Entry")
                }
            }
        }
    }


    /**
     * Test Incoming Message Processing
     *
     * This feeds Frames into a Channel and gets the result
     *
     * Examples from Default Avatar `O Gato` by BELLA!
     */
    @Test
    fun testReceiveStateList() = runTest {
        val channel = "nodes"

        /* State List Request Reply
        *
        * Request:
        * val requestStateList = """nodes:{"event":"payload","type":"stateEvents","id":"mini","payload":{"event":"list"}}"""
        */

        val testName = "State List Request Reply"

        // Basic JSON
        val replyStateList =
            """{"event":"payload","type":"stateEvents","id":"mini","name":"avatar state","payload":{"event":"list","states":[{"id":"3","name":"#1"},{"id":"D","name":"#2"},{"id":"16","name":"#3"},{"id":"1F","name":"#4"},{"id":"28","name":"#5"}]}}"""


        // Create Decoded Generic JSON - this just verifies the JSON is Valid
        assertDoesNotThrow { Json.parseToJsonElement(replyStateList) }

        // Create frame and 'Send'
        frameSender.send(Frame.Text("$channel:$replyStateList"))

        // Create a Decoded message from JSON and add channel
        val jsonVtMessage: ResultMessage =
            Json.decodeFromString(replyStateList)
        jsonVtMessage.channel = channel


        // Get decoded
        var lastReceive: Pair<Connection, ResultMessage>? = null
        while (lastReceive == null) {
            delay(1000)
            lastReceive = connectionListener.lastConnectionReceive
        }

        lastReceive.let { (_, resultMessage) ->
            println("Testing: $testName")

            assert(resultMessage is ResultMessage.ResultMessageWithPayload)

            assert(resultMessage.event == "payload")
            resultMessage as ResultMessage.ResultMessageWithPayload

            assert(resultMessage.type == "stateEvents")
            assert(resultMessage.id == "mini")
            assert(resultMessage.name == "avatar state")

            //Payload Check
            assert(resultMessage.payload is ResultPayload.ResultPayloadStateList)
            val payload = resultMessage.payload as ResultPayload.ResultPayloadStateList
            assert(payload.event == "list")

            // Check Expected States
            for (entry in payload.states) {
                when (entry.id) {
                    "3" -> assert(entry.name == "#1")
                    "D" -> assert(entry.name == "#2")
                    "16" -> assert(entry.name == "#3")
                    "1F" -> assert(entry.name == "#4")
                    "28" -> assert(entry.name == "#5")
                    else -> throw IllegalStateException("Unexpected Entry")
                }
            }
        }
    }


    /**
     * Test Incoming Message Processing
     *
     * This feeds Frames into a Channel and gets the result
     *
     * Examples from Default Avatar `O Gato` by BELLA!
     */
    @Test
    fun testReceivePeek() = runTest {
        val channel = "nodes"

        /* Peek Request Reply
        *
        * Request:
        * val requestPeek = """nodes:{"event":"payload","type":"stateEvents","id":"mini","payload":{"event":"peek"}}"""
        */
        val testName = "Peek Request Reply"

        // Basic JSON
        val replyPeek =
            """{"event":"payload","type":"stateEvents","id":"mini","name":"avatar state","payload":{"event":"peek","state":"3"}}"""

        // Create Decoded Generic JSON - this just verifies the JSON is Valid
        assertDoesNotThrow { Json.parseToJsonElement(replyPeek) }

        // Create frame and 'Send'
        frameSender.send(Frame.Text("$channel:$replyPeek"))

        // Create a Decoded message from JSON and add channel
        val jsonVtMessage: ResultMessage =
            Json.decodeFromString(replyPeek)
        jsonVtMessage.channel = channel


        // Get decoded
        var lastReceive: Pair<Connection, ResultMessage>? = null
        while (lastReceive == null) {
            delay(1000)
            lastReceive = connectionListener.lastConnectionReceive
        }

        lastReceive.let { (_, resultMessage) ->
            println("Testing: $testName")

            assert(resultMessage is ResultMessage.ResultMessageWithPayload)

            assert(resultMessage.event == "payload")
            resultMessage as ResultMessage.ResultMessageWithPayload

            assert(resultMessage.type == "stateEvents")
            assert(resultMessage.id == "mini")
            assert(resultMessage.name == "avatar state")

            //Payload Check
            assert(resultMessage.payload is ResultPayload.ResultPayloadState)
            val payload = resultMessage.payload as ResultPayload.ResultPayloadState
            assert(payload.event == "peek")
            assert(payload.state == "3")

        }
    }

    /**
     * Test Incoming Message Processing
     *
     * This feeds Frames into a Channel and gets the result
     *
     * Examples from Default Avatar `O Gato` by BELLA!
     */
    @Test
    fun testReceiveThumbnail() = runTest {
        val channel = "nodes"

        /* Thumbnail Request Reply
        *
        * Request:
        * val requestThumbnail = """nodes:{"event":"payload","type":"stateEvents","id":"mini","payload":{"event":"thumb","state":"3"}}"""
        */
        val testName = "Thumbnail Request Reply"

        // Basic JSON
        val replyThumbnail =
            """{"event":"payload","type":"stateEvents","id":"mini","name":"avatar state","payload":{"event":"thumb","state":"3","width":129,"height":129,"png":"iVBORw0KGgoAAAANSUhEUgAAAIEAAACBCAYAAADnoNlQAAAgAElEQVR4Ae19B3gVRde/UpKQUENCJ/TQe++9KFIFpUkTBEFRQBBQQIpiAZWioBRFEAQVpCkgSJGO9F7TE9Jvbt8ys/9z1tl8w7h7k/f9PgL/980+z3lu23vv7sxvTj9nnnoq98g9co/cI/fIPXKP3CP3yD1yj9wj98g9co/cI/fIPXKP3CP3yD1y7OgB9CvQGKDA3OH4+9C86XX37962JjQ05CS8vAq0FCjoP/V+8zIQJANFA+0EKvbfDgLqSRveoF7dxMKFCqXBy4tAp4AG/Kfeb02g+3nz5onKly+fDM9ldsPt/ou5QNCQQQMO+vn50X59nnOvWvHpLnj7R6A9QCX/E+/5+6effvpSpw7t7n+14rP05s2aeOE1AuEBUMf/RhCcP3VkWonQEDUoMJBu/f4bAqBwjB4x9Ja/n999+Ljpf9r9VkUxUL58uYhN361xws2qv+34kdauVSMC3leA7ECzgQr/N4GgWZNGx4Er0jUrlyEAKBCxJ0dH1qgengjv34ZT2v8n3e/8PHnyuL7+cmmiN+OBAjer4U3v2/1zCtzsNfhcBYoH+ua/RWnMmzdvPbh3Z3i1qjDxMSoDAT7GzZw2+XzBggUdcNovQAWfbJm244W8QEWyOO1poDuVK1W0Kc5kB7tZBIFG3GkEgHC9SOHCyUxHQMWo/38DCPLnz781MDBQXbRgTgaMhcfgBCgS3OnxR8BayGAK9NBszkUVoHKPAwSvAu0HqujjtFZAzrGjh3vYTWaCgJG6b9fP9woUKHATzotkymK1/7RJh1UfFhJSfCA8ToGXy4ETyE0aN3QDF0iEMfCycUFCThk9oH+fiKCgQATCz0BlsjEXHwItByqf0yDYCXQb6CUfXOAXuHH10P5dTg4EIhCU33b+lACmUrKfX36UhTOzu6DYfyDLxFVQA2gI0ESg54FmAT0H9ALQl0BrgQYCTSlQIGB2ubJlPgXw4ftzgFYAvcH8GWFAdapWqdwRONhYmLBnmXx+GQjvdUyxokUWwjV36N61c+Njh/YOP7h3x7Lln320oEe3LmM+ev+9nR8ufO9k547t1+TNk+frokWLnKxTq2YqyHpXk0YNkmrVrB4HkyxdOXcCF4YEJDNRYIBASoq5c7Na1SpJ8F+XgHqye7Wah6pAx4B+AxqV0yC4BXQCqLfFKSjfo2CwVdWVInEgMOMIytqvlifVrVMbrYXjQCNRlwD6gE3OW0CTgMYBDQcaDbQZaAuTnWeAjrDvngLgXfL394vy9/e/VqRI4XhYhUkwmYnFihVNKFqkSHJwcLFUeO4tVKigF86JLxAQcDswsMBfAQEBV+H5DWDV98qULmWvEFbeDqw5AiwZdORcg9+Ig+9ElyxZwtWoYf2E0qVLZZQvV9YO9+iqVLGCo0zp0mrpUiUJvKdWqVxJgUcvPm/SqKHaoX0b8uoro9WN33ytUk8aAsDNuIDKLQ7CXqeCyXgY/hOdSDOAPgbyt5iHtUBOoESgzUBNcwoABYGSgGKB1lmc1gXINmLYYIXdnDH5MkM8DwT9/Y/en5dYPLgYmkhOoDT26AZyMUIWmQIcIwNZKiicErxGJcoBqzsVXt+FiUwFVioHBQXJpUqWcFeoUN7dvGljD0yat1bNGq6qVSrZn+3R1da+bWs3rEpHWPlyMky2DJMlV6wQJuPklSxRQoXJJ2XLlFbr1K6p1qheTYZV7K5ZI1xu3bK52rJFU/J8v94y/I576ZIP3cs/+1je8dMmFTiCcuH0UeXowV9l0Hc8Rw/86oIVL9kSoxTQiYwJVhkHcDPiQWAAQZYdSbe6del4gAF7MxtPcR4aAsUxEKQBnQRamFMgeI396RWge0BmXq7JqPmPHztK4W5UEwDxEBCS4+7SP//4TYEBjTnw2y+RB/fuvP7F0k8SQIFKWfH5J+kL5s6KmTV9StSLA/rZ+/bqqcybM9P55bLF17ZsXJdweP+u5B83fRsNE5FxeP9u6fSxA1JK/L0MsEjk1IQI9d6NC/ifSmr8fQ+sRAW4kwLvy5vWr1a2b93o/n79ave5k4c9929elG5dOeuFyZRO/XlAAnteibh1SY68fdnjSouTM5KiiTM1TmWr2MMejXtUOJArHNiJwAlVBgAPBwweBPi9KLiuvTCGJ4D2A00Q5sAP6Cc2D8lAEUA3gX4FKpETIPgGKAHoIpAd6C5QGK8LMfYsrV213CVMOuFuVBQLGr8auAEyBtgryFAqiBgeWNQMaCbf4ydPFd4jJr9NTSaW/y3i47p4BVAkWfieG8D2A4giFIvHgBYw9zvPjXH8jwPZGFe+yEDQ8VEDoAZQFFA0+3MDicuAugHlQTcxXHwCyErPiSP73MIqMMgwjzQLspoA7X9BZgBQuYmwmrSsAGUFMA8n9w1gO9l7XjYGsewxgwOCcW0PDv++uw+M519AW5nSys/FQGT/DADxQEeApuYEF1jKuEAyA0IiA8VhoF2MI7wGMltt3qyxJy0hwsMNAuFu8BxQgo/JzWoS+PPE71h9Lv6uajHxxnseRoqJQkuzySkIBzKceBcjNwNEPBsHO8cRCAeac6DboPVyAWiUyXyUAvqeiYZvgRrnBAimAy0BOgd0hwEikl1Az+plC6HZthOUNNK+XWsF5K9bYLN4YymIcjYYHkFn8LVyrSYsK9ZNfQDE12rOLifI6rfFBSAxQLg5SgVKRoeRMF6poK8gCNKZNWQ2J6FsXnoxTpwjiiEqJWsYCzoDtAeoEvu4OdBt5ASTJo5zMbQrAot0sPfws0g2AFYDTQR5SyzYOjHRFXzpBzlFZmCQucn3clwhhT3yHAo//7N2rZpxzBQumY35QW/uGKC6jxoI3YG2MVbUgPuoL5pyYLIp23/cmMpWO79iJU6bTgOKYCzRKbBcX0pfVgOuPYFkphxKJhaFzURJfLD4wwVn8+XLmwJj2zWb1huajkMeNQjyA70NNFb4aCnmDBQtUkSOvXdNYiAgFmzbWBHEQj7//zCx2Z103tKRudXvYkohckd0I8cwIPDf9STH3r1atGgR9JV8ncW8tGJmezzj0Hlz3E8OusDRAH9/pV6d2h7JnigxtBMTuW42mP+uJUD/jdXoS7Gk2dT+ReLFUzrjdBmcgmm4ij1MGTQoCugu0H2MHTARaRNEouPN11+9xryIBS0AUBroAlPU7zOdrUNOY8AfTcMCBQrI48eOjmEoV0xYu/YvKFtWXMHqu6qPCVMFjkN8XFNWE05MLABjlUvM9Etgk3oD6DZb5anss7uM7gHdAbrFnuP5l9g5/P97d/68KSYoMDCZxTDMQNCceQ2PMt/NVTTb/69FQN0s7FAMtDhBKfR+uWxxNCcKaFaau+JMpinx92j03av04pk/6XdrV9Kd2zbTK+eOk21bNtAfNq6j82bPJBPGvUynvDGRvv3Wm/T9+bPpBwvm0E8/eZ9+9skH9Ns1X5K1q5aTk0d/p0uXfEhBMaWjRgyl1y+dlsFKET15qomXz8uvVtmRJN+++hfdvX0LgetRJk+aQN6e+gYZN2YkGTJoIJkxbTI5dfR39ctlS9T1a1eq361bRbZsXKeeO3nYG3Pvmv344X1upB82rLUf3r87/dD+XY7N361xwPWlnjiyP2nPL1vjPl+8KPH1Ca+kTp/6RvJrr45NnTdnZso7M966Clz0NruOTCCAuS3VrBGObvJdPuYILYWfGTeIAar2fw2CuUzONLA4BYM9bn8/PzXq9mW7eBOGTuCxJdBD+3aR1SuXUhgEOmzwC7Rtm5a0YMEgDfQJJJo/f36gfBQUTAqgovgeiBoNCbiNhpdjRfzn+DywQAG6cN67D61cBB2nlxir14jqyc7UOE+L5k0VuKbMa8Frw/+H1/rjw9eanxrXlv/va6X43Lh2f39//V6AS8J9FqShoSG0RGioEhDgT+A9oAACv0HgWuVSpUo6Br84IMmZGmsTOB4ZPXKYJyAgIBburZSPefqAcYMNjyKJZC/QZWYRhJuchvaso0RoiAKo9QpaLq46z9dffk5qVA/Hic3WhJpN7L9D4dWq0IHP96WNG9an+P9lSpeiVSpX0p7t0Y2+POol6blnu6sD+vehr74ymn768fvkrTdfIxUrhNGgoEBarGhRWrVKJf17cB4Z2L8Pgd8inyyaL095Y4LctEkj2qVTBxIaEkKDg4sR/A7eH/4vgiCsfDm1bJnSpFLFCvi/BH+vTJlSeB5hAFERAHAuQf8KKNUeuLZUV1r8UW4h6eLqjYnjSZHChTElb5CPuQoE6gz09P81CF5gAIhhySTNTE47iNG+bl062Zi2K3Oy2A4sOf7ZHl311QWTSgsXLkw7dWjnLV2qpFI9vJraplULB7D19Hp1a3sqV6oIgxamNmvSSKlWtYoejq1Vs7qKg/g3d8gDA5yHcYsCtFixorR48WCdm+Dn+Ps8CBBwfn5/r2CecLLYb+lkrPjAwEAKq5SWKlkCqCT18/PTSpQIpa1aNiejRwxD9i9zbmFdjCAoalSvpuL94eTrXAF+D6+redPGat/ePZURwwbJ36z+Ih3uxwPavlqoYMH/mXx4rd9PQACpVqWKBKLwPhvHTN1n9ZdL8ZpsLLyc45lEi5iXMIrJHDGdqTKmksENeaZPnZTImX6UPU+6cPpoWuHChXT2COfCCvDDVWVr2aJpPAAgtkO7NjHvznzrEqyAKJCJicCS09MTI1OuXTiZAfI55d6NC7EvDOjn7N61kxdWnrdm9XC1ZIkStHy5sjQsrBzFMO+YUcPl79evdpWGlV6oUEF9MvDy8Xmd2jVhMprQ5/v1Rn1BW/H5JxqKJdA30o4e2JN+9vgfTpTnf508LE+fMknFFW1wIRQBOLnFg4vRns90k44c2GNjip8tIyka7vkNZfzYUTJMOLJ3XRwgmy9ZIlQFjqLAtQIAhnj//OM32287f3QBx9C5Bf42novXaTwWDApCruGNunMlkgOBThG3LpFyZctKcO6eHM9BhEkfzfSB80DjTU7BrJ1EuDjMJIpkppFx8cjSMrwZiTZcpQwEuEJxlajAZpNgIjNaNGuSAGCIV10p8UyTjmLOJN2EAmUvEVivG2P+CCa22nR5jKnbJUuWUF4ZM9IGyldq7Zo1QHb6K7CyCP5X184dvaBwJnFuWf7aDK08mZl2ST9tXh8NbJewSdK5ROFChXRggXJGZk6fbAdgnketP/1BZPykCeMco4YPdZYrW0YBcGdOKMp64GBSx/Zt3QCAeG/GAxybiF49e7iA2ygwFoZIIAb3Qv2hbp1aXq898RbzHWQq1zA2pEe3LiqM222WQZWjIGgN9B3Qaov8AUwLc4FiIwGCL7DVbyiEONA2EAfOhvXr4qRlsmlkuagkwYqWgfWnDB82KGbzhrV3YIDRTDoLdPjOtXOXXxzYPwMUKRlEh1TwbxZqKFsEBx3BFRJSXJ37ztt3AGxHge2mAdtU4Hp0EFSvVlVVXampQoBG5YI6Ucw+R9BFXL94Kj40tLjKK6X4P1gsAhNNevbo5gTL4SScuzk+4sZx4H5JvZ97JgXkvz0AZLwxoXCvKih67k4d22XAxGeAEhwJQFh//+bFrSBS9oH4SIT7UBDIxpigmKtft7YKFkYE4zYSr9QOHfwCgUWACTYLH4dI6MVAMMHk488xaxgGyM5WFZ9KLbEomeuZ7l0IY4PUkNUog2HwSKMG9eUhLw50jxg22A0iACfsJoIgNf5+FJiFGcABJMwEAr1ABg2ZoGaNIMJHYNUE2L0M5ht64e7Pmj4lDc5VECj4+7Penmrm0FGZXHczx04cc9jc/2D+7FTMNEIAIBlsGycL/xuu1Q0TiXb+mZ9/+G433Fdk547tozt3bHe3SJHCbsytRE6Hsh5lPogWb6OG9aPnzZ5xBPSe37f/uHEPAG1r7Vo1EkBXQZ0gc2EEBhbQAFCUuFOTWaCNz0AiqLjCopHgP1bB+X6PAwiNgIqafIRVRmA/D0jngiB8goiegfPhwrkS3PRDShsOMkwYRaUKNHRvN2Dd27duTFvy4ULbpbPHHix87530fr17OkGJdLZq0czVrk0rF2rWSH9zgQIEVh9q3WTkS0OQ3cePHT1cAkVOH1yU5QlRt1QfDiQ+sQMH/j6wd1RQvXitBms3RA9yA1BWva+8PFIHKnCtQ3C9B2Dlbnxr8usnWjZvGgEr1Q2TibIbOZH+XRBZoMu0v/XxB/MOXrtwavuCue+cAPEXgfeNFoOxKFCR/G7tKsJEUyxbQJkA/uvEIRJWviyBcficTzR5Eo4ziPg5s6bbRfbFR8/AXvfiIPIgwIHFiQL2TUD71vP8QHuWhgwa6Bo1Yqjji6WL0YmSnhx7NwZ+P7lt65YJoHDJOEGGQoUgAO1aAaURWbvto/fnedAsQ20bwEN8eBJVwXmE34+MvHU5qke3zg7U2I3/wInMly8vgevHRFJpwdxZSccO7Y0GcRXhSos7jeILlMT936758tewcmVTg4sVxRxJXQmG78ghxYvbu3bucPvw/t0/w7nb3Onxu0DR+61/316KYdEgZxvy4gDKRRCTubwL3UwEcUIqhJUnAMJvnyQA1EMnESphq1Z86hBEAe8nkMGM8qI5Jzh06N8TmoeiMgZyXwUZ6unetbP7tVfHZuDEwgqzyY7kyLSEiMNgy9+GwUJWrcKKU8DMQk1eN7G+WPoJTqJ903dr3Jj5i4P65muvqj5cxMb7XrbyEpiLNxJWY3zDBvU8eF3GKsXrBnaPVokrvFpVB4ggN4gBR7OmjT2TJ01In/rma2k7ftp0CN6Pg/9Ph8mVDCUYOSV811W/Xp3YP/bt/AXOvQCWhr1B/brEUD7LlytHwQrir9HNQsyEjyXAQiAtmjc9BM+fflJAgJUydlyJkbcuuYSIGQ8CZcvGdW4YMAU1bTNnEHIFtJMb1Ksr9ev9nAsUKQesSBcMlhNW2WVQLne3b9c6tnGjBjGgdT8AZVAGAKjAekELr6GuWbUMQeg4/PtuN64WuCYKk+LOIk5g6C1pDAQoh+/BSr0LOkZ6hQphKvP+6XoBaPUEVrUEuoGEAEbCbOUmjRt6Gtav59217YdLcRE3vgWL5Aa8FwH3pDCOgKzeAZzpNoDg5+WffXS4W9dObmDrMpqVoL9oAHoiTjjn0s4ELgBO6dPrWbyv3k8KCLA+QKpapbLiSI11CUmiDyV+ZCRFKZh/D9yAmHn2kCPA6lGrVaksD+jX2wkD5EZN/MWB/ezABjHQ8gNYEJdAyYqpWDHMBpxABwD+HshXdff2LajoOefPmSWFhhSnHdq1UewpMbKP7CNeJOC1JzGdBsFwD+T9vTdff9UJK9gw+XTRgGSwetRpgsC2RzOvZvVwGThWDID1LOgn5ypWqJAEogosmiAvngurncA4Ofv1eU6+eOao+4P5czzw+6js6n6M33b+qJoExlTB74IxCgU5CIihxU8KCPYgq2vTuoXiSo/nk0aIkDmsD/i6r1egd5Bw/gLtf5wyerxAhhWuhBQPxvPcMGCOnT9vRns5+typwyeBC8S2aNYkCmUzOpxKldQLPnQ5ueeXrZ74yJsuGFBcrWTRgjmKwJlUk0wlUUl0Mj8H/mcE/F7MyJcGe9AXwSuJBsF1ECN+0L5ta8+C995Jmw86DAA4DauOChUqpKAFYNwnOrLgPCzBk4B70dCQEA2AQd+e+gaV7ImqCcdSGAgygQzmJQHlVPlm9RfLnwQAYAOKBJTPgGjJJA1MTN9WwfRRQKlB3wAVYwK40tCPjhMMg06whv+5Z7u7QPNGLTk6NSHiJIDiVnjVKimgSKowoGqlihVUYMMqggB0Ac+Ilwbrrun6desQYMtmIJDZBCsWqeNG0oeDiQiUybHr16z0VqlcUXdbo0kKokZGnQQdPgaXwGAVKrjjx45WwHS0gS6T2qxJIzfqEYZVhPGFypUqkBWffyJXD69GQXfQVq9cRtMfRFKLNLl/JOXcvHwWYxvyT5vX78OGF48bBJ9h0AgGwrVh3SqHEDQSQZBpjp06+jtp3qwJKoIPBYdw8kAD1wkHDTR8GYtPcEUCl4nav2f76e/WrboA7yswCAT9CzXCqxFQztSO7duonTq0VTEmUbdObRXseI+g+RNuZeFvSqBnKMA9VI/tgVUegRFhRO6Qii7lvr16ooKna/L4iIQWD4gl7LtAkLUDMMnA/n09YNK64Rol4FTo5FKRk1SuVFFu3rSxt3p4VVKuTGkMg6sW2c9iroRR6ELwOxXCwhRYeNfhddjjBsEiBAFo6FG7t/9gE5JIKDeI4ookD6Jv02e6d/kHEJCtogWBqwuft27V3Htw744HYDol/vTD+pOgsMUs+Wih9NUXn3m/WvGZB1aUAjoCRR2gXNkyaGIp965fMJxAssCdjP/32hKjnDhhSCBm+GxoMwvCyAn0psTfS0eZ/NKQF0n5smUpsHudCyCbBx1ErVe3NoonDJHLE8a9nLF65dKod96emoi6AHoQQ0NDJBB1MnIH0F3U9MRIL/tt/jo1i6Rb/ZrmvjNdj062btXiCrzO97hB8C1MoAOraE//ecBlUZXjYW5ZsdyKYCIJ5hOA3Z8pGjAej5OJmj1yBpzgoYMGKnNmTY8GRe1PxqZ1d++NS2dc48aOQm8hxg7oiwP6IVvlnT+yiYKqv/f+vHcJmmUwIXT40EHol/cK2VBWlVF2Q0Ynx91TJ44bQ9q2aYXKqZ4rgNeM146iCjiUc8yolyIB7GktmzdNAo6pBBcr5gDRlbxp/epYUCC9/Ao30QdMQYAtbmpUD/eA6Pn9cQMAExuOArplUNTSL/91XPJRF5AhhJczb9iZGktgZRNgnWgeZrpodaUrf34NZCbt1qUT+ebrL9yYnQMDJ1088yeZ9fYUgnH+hvXraggCWG16gEVQSGVBHOmghPPwmvXV+8Zr43U5fubYQTuXIq+aTAblqogeWrlwTeTEkf103pyZdNqUSaDH9FDQzB09YpjrpaEvusCud4OO4gFFMGP2rGnX71w7d5dlWXsEMWCVG/mQrnXgt19UED1OEEPnmJ/msR0tsWQb7GYJtfhzJw9TiyxbiYHAxshtYkHoNH/uLNSWM4NMCIiCQUE6EGD10FKlSuguZowJ4MqrXasmnQ5aNSiAVnl/biNuwecBRt6+LKG236VzB/TAKTVqhBPQJ1JhMm8JBTFWdZSirvGQgomgUJzJOlCSYu7Ia1ctl9CBBQqug4FINgFbdmoW9P+4efmMWraMzi2xYLXc4wQBOipiQKZ7RgwbnBwfccPKEePhyq+cbFKsSryoHczs79auou/MmKr1fKabBqxa69WzhzZ08EANbG/63uwZdOXyT+mubZsJKH/URzGK6APIdGdjFhFym62bvkVW7Fz71XIX+ifAvEs20SP4BBJJ8IMoZiDIou4xq1qKLCuhULEGy4mCcnwP5qDZ4wRBa6A0kE3p+/dss6U9iLACgZdjzTI3kNmtJvY1QNktHzNC2gRMN4Kyu369Ohip0/P/0WydNHEcKR4cTJLj7no4ZZZ3KRvOpBRB2ZV9VEiJmdYkm0U0xIe5SKPuXEHTmILJipVJQx4nCMaBKYfyLoVrwERNZJjMDZYspJ0RCw+ZN4sM5axSxM0GX1+Nr094BYNKFEBrrEydK9mToyU0/3758XtiUt6m8rkRhmuZhZ+TTPIpiQ+OkBUXUARdhoq6ASjUKpiZtGiRIlee+rury+M5wI5fiSZP184d3BamoVn5N18GbtW1hLD8fCk76eo+BpuYpJTLzrQ44kqLN2XXidG3CchtalFYQgVdwwCEg5GTAUoWOElWRbVm1yKbmIyZ95OWEKGiOVqoUMEHVoWqOXE8DUrJBn9/P4VzzRKh1k7l6vMVQRnkb9qMFRr1+5LAeqnJIImD6OV0DoeJfiBnUUUslo5ZgYFYxB+8Jh5KhVOSVZMFInO+CLfJ/fGcQPXYEtTuXTvLpUuVSoS52PS4QJAHNPf9WG109OCvNpPKHn51mHntqEXrGsqx6AxuUGWuilexULKIUAUkm4BP1bJfxm6ls2QlimQukYYvQXNxJeiScG38ebKJzsQvEN0bCmazt2SJ0LtP/d3T6PEcefPm/RMsA+n4ob0OYYJVrvmCy0cET7VQfFTOHncJq0gMs6pCYohpMotJAIn4AOG/09/AzFWucL0InIz+ADrNrXZZSHPzClzPNBsKLDFbzRrharOmjZNhIW58nCA4HlI8mPy46VtFsKttTGm6J6SaiaXZsgVnECdX4vz3qgkLNlPkiI8ooVUEkWQTDFo2mlfIQjKrMcmxLKFV5kxOkk3Rk3md8ZE30sOrVcG8DKlc2TIHHxcGeoJl8AB79925dl6cTBu70RgfWr5iYlGQLNrJSBY+ANWCxJiFR1DeZB/cgQiKpVkQSrFolMErhQrnarYzS0JUHq1MSf6/nPz/eO2JtlYtmkkAAgxGbX9cIHgjX758jj69niUmLlaZk4GeLNrM0H+RFBP2rnIrThL6A3m5AU9nk5AsyGsRSA7B1WyIpjT26Ba+a1yTi5tg1UJcmSmc1EfqG5/fkKlDKM4UqXGjBgr2bKxTu+ZjA8GSPHnyKA0b1MOKYtWic5fbpD+BGYvLjkNI9COYcQRJeFSERBGDHExfieTc2GJRqpfrNKZwv2G4oB3co9uk3R7hrAW3iWfRqnGHxoWNvZy1wHd8wa6oau/nntFzGpo1aZy0f8+2nM8pAMvgh4AAf3Xk8CHoJzfTjsUB8mXLW4Egq6AKEVaxzJmWLm4QvYK72mDP0dzKdnCfi7EBUQk166+omLyWucl0CRMrcyAx02U8godVzNdUX3l5BClUsKDSqkXzlPdmz6iV4yDInz/fT0GBgersWdPEFUu4OEE8Y6Fuk2QTX5MsKlfUR2TNy+kXTkErd3MDqAimo5cDgGLBQbIyGc2CSmbBJf6/HVwcwsWBQxQXYpfUf5jCC997F8PXcqOG9ROBI+R8d3h/P7+dgYEFVGwGIYBA4m4ujgA69YQAAB9ZSURBVLFdh1BAkZ3WNL76GGkC27QL5qTZhCgm8QvJZCIUk4HXshnsIVnoLLzjiNebnILipwgiRTHjBJhdVKZ0KaVqlUroOq6Q4yCoHl7tNlb/TBw/RhwcXpkykj8cjEUTH21frQZXzYZDx8M4TrqJXHcJEUyX0JFE4uoNUjjuIDbZ+FdjGGbZTDKnqComTS7NzFXFpOOqft6USRORE6jFg4NvPJXT+0Q0qF+3bJXKlWyYGYwZsiZ2ssJ155I5uZxV718rkaBYRNX4wpEoLuPHzU2wm5tw3l63M2A6OU6VIdb9WTi0aDa6p2aeQ9ypoivdrG+zVZxB5fQD3lGmJ7AgCIAb4IYh1XNaKayN6VGYA/jaq2OphdvXyYFA9hE3yCqipnKTaOXqNSbbZtYoC9vjYJgbs5EO7t1B9+76iV7+6zi9cekMjb1/nWLnckwC+TfD1Kbh4IzkaO3XHVu1RQvmaJ8vWZRVbEI18bgqQkd0Sfy/N19/lQYXK6Y0adwwtXvXTr1yXBoULlzoLpZnDRrY32wwjAtPMrGbxfg+zSK9inBy32vhfjZAh944p+xIohdOH6Ub1n2lTZo4Xhv8wvNah3ZttJIlQrVKFSto5cuV1YCbaXXr1NLq162jNW5YH/sX0F49e+igxiZY1y6eAtM3JSsx9I9rwf9eOO9dOv6V0XRg/7708O+7s9IhzJ6LTjA352/J/K1XXxmtYdr9CwP6xUTdvvxmjiIgpHhwj4oVwtIwE/idGVOt7HgXK/NOFApUzUxEyVcT6ZT4e64Z0950LZg7y37gtx3ysUN7VVdanOpMjSO4wi/9dYyePLJf2bZlgzx86CC1fdvWmNaNad9a547taZ9ez9L+fXvRZ7p3pd98/QUFmxpd3XTtV8vp+jUr9fdWfPax9v68d7VnunfBribYN0HDZlrIci+e/ZNaJMo4eR8Bbmzx5dLFtGP7tgTGRXakxCjZaNVn1paXmLT394iLpG/vnnoFMwA8KfL25Qk5CoL69eoMAGXEhd1CMEkTZJ4ZCNJY7OCBxQoWm14rvrxmh/bv0ptLYZ1BzRrhFCt3ypYto2FW8t99hUrojaU6tGtNZ0ybrGxav1q9duHUQ7Y9+jMY20f2r7fNE3wc+j0cP7xPGzdmpFalSiXsfKa1atmMYu7jnWvnRDZu6Bl61vGYUcP1WgoEgNuWYOd0FK8Pl7howajCdgApXAPMh/IRB/bvg4UstGmTRmmvT3ileY6CAJSRDlhxFBAQQGe9PcWKEzhY7MBpwQnMtGfqo1ZQ16xh1Uu7t2+R33t3hvrpx+/jhGurVy7Vln36kbbl+3VYxmUWXfRw3r9MNo4JoO70BHTBGjrBQ9cIugIWs9KXR75ES5cqRasAd8EUtHMnD+u/CyBSEqNvq4sWzsWmWnot4ciXhhDsfcSZrU7ONyAJlkI6q4BO51i+V+j15OKaYGdyA7hebfzYUTSkeHEsfrnwOJJNXwR9wFugQICGRRA+0sM8XIKnYgECagICMYhiaPRRwm8SH8ob4Vaq18KV7OWTXnBbHCEAlKmcXTl3wtuzRze1bu1adOqbr5GF771DOndsR2AVAmeqTmAlKlfPn/QKPgljJbuEmIPxaGcc08Z9Lgmmr4sDg2SMM4CXomKIBSiwKE8/5aOv4aM6RoOFIKHsxK6hFn4ClXPGSEIqGfWhH5gFVxQ2YFFCHD4rDd7Lu67RSsDcPFip6uIPF6oTxo0hHy58TwGOginhuDWfAvoGuXfjgupOj/fC+V5nWpx868pZaf6cWR5QHCXcNAvL36pVrUIbN2pAQSmT1329gp8sj5D/4OY+c3PBIDu7NhvjBHYTF7si+DkyU/Gi7lxBMGJJvOrv738xx01EDCPjbmRBQYEatpz1AQIPd/GyUHhilZItetf40uxUIcnEqufxQ2lfWGNw9sQhBRNM27VpRZvA5NWuVYNWD6+GVgJt1qQRKo7K0MEDlapVKpNSpUpiOzxsB0Mxtx/L20qEhmLzDAJAkHf+vNltS4x0bd30rYJKKxdz8HJavCjvZS4aaWRdSVxNhsOkEEXlRIGd61GgxUfe1N4CEISGhJDChQrdfhwewz7ACbwIAqwPsHAW8exWdNkqPkDA1/15hBg97/Hz5X0kRr/kjd98TQf0641t57DmUANlVmvbuqUGcl77dvWXFPsgJ8XeJdF3r5Kk2DvAIRaojRrUo2Ay4oTrZuOIYYOxFxJZufxTAgqgwgXHJCEgJHIoM7CLnkuZ4xZmIJA4TphZ5geWh4Z9nrFPE9Ctpx7DTuu4y7mEzR6/XLZYtIMVk8nmV4XbgiOIDhI7ty+QyplJHo5tEotNttTIW5dIl04dUGnCSmcNTUZsdo0l4GjyAUBMaxw8GQ80MD99dTg3C2PLJiVv1IdjyyUErySLYhwiiNLMukpUgNGngW37SpYIvQ2Lsl9OgwC3jHUj2/z911/MAjqyydZ1fGMo2cQdLAZtVEGrlrns4XRB/mYOGqwQFX3qtcGExAqdWjWqY8ErAROTz0JW/sW9Fawyfsza6Fv1RyKC88fFXUumO/urLz5Xzp06TE06vxG+7hHMcvLKyyOQu6lNGzd68N26VWNzGgQfATmxmnf/nu1WokA2SUFXOEVNDPMqJokXvN9cETRql5Cw4fnrxCEntnAJCgzUmjRuSH7dsVW5e/2CAkqemHMg/xvb6hAf/QO0bATFCAdcl2AlOO3JMa5pU173gqJJuQ7sxCRNPbPWEcQBQY9hlcqV0r5bu3IHvJ8/J0GA/fPcIF/JpvVrRMR7TFa5WU2i3aSmTzFh8WJYOE0IFOlgWrNqmVy6dCkF6wsH9O+DxRketiexS+AYkiDLSTYCQlllQmUnG8oIhqUyTpbKvKm6L2HBe+9I6PgBIBCL8PRD3AtBgP0RSgIIwGyNO/3ngS3wfsmcBAE2S3KB1kz279kmrqpIdpOKidbuFnYBM0sS9dVr0M08kLo4UF0p7huXzngWzJ2lYAdyMNtkMPcuR966fAcGKZGdGy/4CVShwNSqjtCXLpAdEJjdg4tzXDli7l1zR9y6hM2x0dKiI4cPwe17VYsgkyjClEEvPK9WqVQReycn/rFv57c53c4Om1m7cbOrq+dP2AUHRwzbzkUSBoxyCpDXAgSyRckXzxb1ps8wgJ7Zs6ZJPZ/pqmCzyoph5dVFC+bYwd6POrx/96GMpOi/AAj3QXZGGSDAvgacQ4jPN5DM4vXCtfH7Ilv1QlQtUsR51u92pyckwaS5wOKQUWHF9nV9evdUbIlRXovgmGhRoQvcjp1QsC1P61bNowBM6+D9MjnqLAKyFypUUD5/6oi46lNY4Ej3nq1a8Rnd8M1XGsg8TajMcQupXYqFV9GwjYnkSFJ/3fFjysTxY5LBxsd28KRJo4YEzDpsX6d3CcF+RcAVXLA64tq1bXW7WZNGyRhzeH/+bH1X85S4e7pixUjmtHSvhQOL9y46TfZ5VH2UnRMhzxD7ITvfe3eGp3atGgr6H7CtDV5fXMR1j4WuQoTmHpQBOmP0iGFSn17PxgJHuHz94ikUBzmqHD4PJkmqn19+bEeXYrKPX2bbl6SYO/TDhXPxRnHfIuWHDWuVE0f2e0FmiyXemcGTqDtXSOTty9QLZhCyx+OH99FxY0bSihXD9J1FKlYIc8OkSzOnT5Zi71/XlSZZB8jW9LenvoHFGDKAwRUQEOAFYChoRhUpUkRvMIXNo3FHk359nqNzZk0nH38wTwVQkbnvvk3emz2DfLF0MU1PjHyIFYPZKOYt+ko+9Qp1ibp1Awq0NHb0cKV8+XIkuFgxvZ0NAmDwC8+T2HvXXJzVpJpkVon7KKPYsL0yZqTn2R5dI4AT3t22dcNueP+tnARBN2Bh0RhE2vHzpkSTbhuUM2dwIsmff/xGunXppPcVwh6DLZo1kQb27yPhIMDKINOnTsKScBW4ht5AokP7NrpzJ7hYUexghh1KNJhMrXvXzmTB3HcSM5KionmOwx4xnyAdrIE0+L9zc995+48lHy28ir8/dvQIiu1w0PmDfQmKBwdr6OzCIBiyY5gUvZdg317P4oZb9OiBXykGiu7duEAQwGtWLiNnT/xBEiJvYkUz4VO/UcS40uI8t6/+5QZRJB349Rf1yIE96icfLlABXMrz/XoTbIuDEx8UFKR7INu1aaXOnzvLkxB1i3cWKRbRxn8ky2AXFGwZ2Kpl81T4rbs7f960C95/NidBMDw0JCQJG1p/uWxxssXu51QwadD0AWtiNQH0qiVLhsKKKIop07RI4UK41xD2DNZgwrVSJUvqzh1csa1bNqfz58zCnc4osDyqgn3MRSgjOPZs7MFs7DGYwLaWSzOuA8CYmfjxx96d9IP5s0mb1i1pzerh+qoMr1aVIhAwJtCieVOKNRU9n+mmu48xEaVkiRJ6Qgqeh+Jn6KCBeo4Cts/DjSjQ1YyhZOBEWpkypXTxFFK8uIbARy9kdfheh3ZtMOdAZh3LPJznUSzX03xUYhEAOgKMdOvc0Q3gvrt968bN8H6OdjyfWKRw4Vg0x6a++ZqD2+XELKSs8EDwZiTiowosX/5h4zo7oFmZ+sZEgk2nNn+3lhw9+Ctm42BKmIIOEU6RE1dHArfPoMK5X68xjuAR6vxM08Xv37yk7Px5MwFAUGyehRtc4YZZQwYN0DfOAnlL+vV+jsBkay2bN0X/AyarPASaenVqI4fBHUn05BVssYOBtYnjx9ItG9fRu9fPk4zkaJWFqxWT3EdZaFtLfS0ofJ4ce5eAnoNcKrVH9y7Xdvy06ZecdhZ1AgAcwk2xJ0+a4OBXm0VxiK8sYcmiSlgxc5Jw5GIgiOcUL5lzyXoFOUqyqBv4n7Z6oDwiGEHfoT9u+pZs/f4b9ZNF83GXNIp7H4KOou7d+ZMK5rEKIgAbYT5UZoY9l7LIHpK4mkjed5GdFj36f0TdvoId39QundpHvzxy2OnVK5euyGkQoJ/6GIDADSzJKdQbahYOFquEEdnCJy9zW+hY7YQaxzlfbJxL1is4g4iPOINZWhfxEdjyZlEES7LR7IK3OFSL1DrLHkbIURKibirz58yUw8qXS+nWpePNl4a82CenQdAf6CSCAFie16JSKCsQEKFljdlAOUzkJO+GTWR6QCpnRvEZOm6LFHVJKDYhgkdTsmh74+F6FUkm1cyySYqYry5msomjzFcPI0PZVrHlDohQb7WqldP79u4ZuWHdV11yGgTF8+XNuwobO4Pm6/QBAmrR5k0kl0lHM8LFCszcsSoXh+BNLLHeQBXKuwinkPGOIheX9GE2wSoX0k03acopRhStopyqkHCictdsxUUcgm6F1gF5a/LrUonQUGf7dq0jQZEumdMgeKp0qZIjsBs5bjUnOFrMgi6SSak3zzolIURMTeryzaJyXmELet4b6RVAQARO4ODIzTmNXIIYkUwqgyShgkmxCCurJiVwisCxJA6AqkXc4CHLARVsVJpfHvmSgjupNGnUIAbM176Po0vJ88gJ0JQDG5n4CJqoXFtbRXCD8k4V2WIVOYTmU2KHNBfnnHIzPcHOrTQlCzEkC0khviKaipAX4RbEjyzkNTq45lseDmRODrgyF1BTTcLWiuAs0sBM1p1oYInghh9S966dMFn165yOIj7VvGnjYbjZA7Ajmhx311co1hjAJJNOZkTIx5MtUszSxZi6Req2h+kJMez/0kzyGqwCROJeiW6LKKcirGYRDLKQWygLnMArAMFjAgIzSyjF+Bzb8qNF0qZ1C9zxzd6zRzfcOhADSDmbcTxx/JgZuJ1r0SJFaPzffYV9gUBmClWSkHZt1m+QWPQA8PhofkWFzqOXmDcxyaKEi/podunlJjBNUFoVE9buNek8ppq07RO/5xLqIB0+MpKM8ZMNZ5czNQ7zCLxlSpdK/3jR/MMMBIVzFATjxowcWjw4WEWX6/nTR7Jq0ixz2946fPQJUkyyjsVWL75AILFWNLe4ohezTiA8QPn/tnOgcbLfcAps3CWEwyVOyXSZdC1RTTiIIiSd2n2IPCpwSg0dbtF3r6qgD2Acwvn54kXYEe3VHNcJAgICKgYE+KfjDmG7tv3gq/OX8TqdASHNRzs5q72TVAt5qZmYXzaO63hNfAGa2UYWJjn+DpYbcYUTLQ5OtrsFwIip4b66ufJ6hYP7vlXvoodyLXBfxG1bN6jFihbBHd3tq5Z/OgPez/mdUhEHTz/99ANsRb/i80/UbCRYqtzAek2qlGWhfJ2YtLAjZu5oAQhuLj3dnUWj6Id6G5tMcBpzQbsZiA2/RAa3gr0c6Hhlj2TBCXj3sdOkk5uYU5BpGaA4+PiDeRgEw9T5E089xiM8T5488bgxxaoVn8km3jmzQIjEBjJRGAzeGyfa6IpJr2Bf7WTcQv8BPq3Mqv2dzPkk4phISWOTe5PrehbHuFkslxpm/FeqwB14Tqb4IBvn6LIqi39IbGIgDGsoypcvi3tArXicICgLoiAFQTBu7ChFWMWyDz89nzXM9x/O4MxBj8AFxHi9bOHqJYLfwCtk+DosrAFDJKQyb+AdplieBzolVA8lcyxcNmlUxTus7NkAgoOzYrLVAQV9BJhf2LhhA2dwcLEljxME2KziJoJgQP8+RGB3vjp7yJxtnMG1tXFwbDxOyMiVBEeMx0Rr5/P4XCb9gbyc9m7mRpYE51MSUzDvc4pbBldo6jGxFmTBI5gugFi0FvgaBOKjjP2hmk10G2NeBZiHUTANn2Zjqh6d1eDv77/xbxD0NuviRSxMMT5VS+Y0eqPnUAqLDCZz2rrYVs5rUs7NT0aGYBXIgrPGrA+AWG5ulIfdZyLACFJlCF3SZO2fO74pQk6i6kNJzCpu8BDnwp4M2DeyYf16uBloDEzDHIvpwb6GKCqwWPXKIwNB2zYt38WcgratW1KTSJssNH7iPYBOobLG4ACxXNdQD9fp3Kxxk1khiCJsmCE6gOIFxY1aWCVeQeOXuG4pUUKXNKsWum4Tv4EVCLLKYH4oQonFqHrSTcWwDJiG7zDdD2g60DdAmGe4FAg/UxndeHQ9a8KrtUQQ1KpZAxEq7g/E1xiIfvdkCxYqc02g7Rxr9lr4CohFObrbwtLw+migRUziHbwjyMvVDbh9tNknQq6Emg1zkVi0yeOrqzOv8fc92ynueuLnp2/EbQeKAMK9mBWsFodH3JJXYe/h8zOP1EwESg8JKY4FnW5B40/jlD2RddtNWr0ToVGTTfCqubPRzo7fEyFdcBapQsq7ksXeB1alZarFSvbVlMoqmmjVOFMzybjOBN3nixfpaXe4c3uhggVTihYt4gkICMD0dU+L5k3Ud2dOc370/rwEfz+/e4wTHH7U+uHOwAIF1H27t3mEyU7mWsJ5hJUis/SwVBNZKnrcXEJ6OskCALwDJ4MrGzfeM/QOQw+RTBJCiEWiCLFY1aqPBt6qj2tWTZRVEQBOzqTWfwf7EtStU4s2a9pYWbrkw9SMpOgHidG3o0FXMNzk6Vh8A4qjDQuHgX56pAjInz//B9jP8MtlS/iwKN9PwGvS6Zxwvnm7lvUWOTSrXEHtnxtsSZxI4uW0GLjxWLSyJ9lIS1NNlFNRNKk+9lVQhV5EIgiMxRTBO8t693xGw2LgUSOGKmeOHYxl4jODU6Azrpw7jo48FAVuoA8eKQh6P/fMeD8/PwXr//h6AyFm7vaxetO5KmM1i8JPXzuKi6lgfGk7NelvLHGKqo3jILLJhGrZaDtHTDq6mymHYpNrl4X307hW5AKH+ZyNpk0aYTs+um3LBi/X6oZfABn9+/bCrfJwl/ZrjxwEwI4qlitb5kHtmjVUITtIFvr0+KrTU7jO5CSL9rFZ7T9EfLS9sVIEzTafULP4v+w0ulSFLuV8+ZtDWL1mlUsGEOKN60uMuUNDQ0O0YsWKajt+2uQxyYRS7Ckx3qCgIJVxAdwuz/9R6wT5qlSudAC3kfdmPBBj8DEMqb4SPlWh7aykme+ZZFYRTLOoC1SzAIPVXoXZ7cau+ahi9hUpVThxZNZIm3cmyXyu5YUzR2nx4GIa1jhcOnuMj1zKzJEkvTzyJS/jAvOAAnMm67TPcz+g02jNymWS4P83XKJ8z/90wS2sCEkZHpPuYUSz3mLWKuzqMAGUqmW9pe2/s/eCVd8kVVBYZSHaaLNowCWmwrlZyzw6fNggjTUMk2HCHRwnwfPSln/2cXq+fPkQAPcfqadQPM4c/2Omv7+f+uKAfrKQ4OnhlD8bVzCSxCllRi+eB5xFYTx/wJl6DhNPobhnovF/cdxeC4pJf2Wr3dm0bE5ydkQB4Wx8j5BP6BV6K5oFzMQ9lNQHUbf06iZ/f3/t2KG9fM2Gnguxa9sPScWKFjX8AyNzNIYAF1C+fr06McHFiiE6ncJqdrJAzB+scaODKTsPmG/+DgOIsTeRjVkVD5h3LpUBA2sPT3KJKS4OQAnsuymMvBYmpZpFe93sgkEzyVeUBBOYTzrxmoBREfowKULbP5co0rC5JtZjYlmc4P9wXDl3Irpxowa20JDiLpiSn3NMDPAH2KRTwFxUfvnxe5vgZctgk/0N0Dnt4a3sbQwQqcI+BR4T/UAWWtgrgivaTBmTfFgT2W1l/69sb8vHKRIYcHkwSJzpmsru3yzGoJhwCOyuptc2Dhk0kK5cvkRP8IVFp+8wM33KpKhKFStkNGpYHzfL7PG4goqY5OgENN6AC4vhbtYwweK4dC2rRhTZ3RM5O2xb/+2Ye9fIqT8P0LvXL9BbV/6iWFGcnhhp1Dh6TDqe6hNF3KlyQtRN7L+g3Ll2znvryln33evnvXeunddDuRa+BFmoLZRNil94vSWrLXUeej8+8iYdM2o4bVCvLq0QVl6rXLkifWnIi+kXTh890rVzx7gO7dokdGzfdsZTj/lYnSdPnsjDv+++yaFcNbGhfe00qv0viTpSYvXNOGqEV6NlypTWsJUNFo6CFUNbtWxGGtavi53QldEjhjp6PtPN+8qYkd6xo0e4+/V5ThoxbLAbNG+pQb06Mpi9Cgys0qpFM1K3Ti2lUYP6niaNG6pDB79AcOuZL5YuhucD6bTJr2M/RJLFXg7/imlpGVLGopO5776tlixRgmJZPegIEu6GBtd9dsobEwcFBhbI97hBEAZWQsyggf3RlWk3aVej/V8SNnk2AwFm3lw+d5w0adRAGTd2lHtA/z4e3EgSdBYV2CmBwVMLFy6EhRsElCwCJpfq5+dHcROP/PnzEXhUsPcC1lVgXn/x4GAnnO8pERrigt+wly1T2lmgQAB81w8bYnhfeL4vuXr+BMlm8ypTq+PM8YN66b3x/k+b1+O+CxqY3WJYWecyoAS627Rq6SlfrpwE93QU7qnsU0/I0SCwQIEHMHgadicRJ+jyX8f1HUHE9zetX63F3r/+j8nEfDo8H1a2tn/PNr2b+cxpk7UunTpobVq10Deu6P3cM/Q0sHsEhCfjAWbhOuIjb9yG37wRXq2qDeTnrWsXTqFCeQZoTdz96wNHDR/apmmTRk1Gjxg2cvjQQS+MHD5k8Ikj+z6aPXPaWvjtyXAfkzA8C98fA5Ox+ubls7/Mmz1zZ4d2rbETSD0gPCcBgBENq/JiasL9zA3Dua1usuJuD322+sulWtPGjbTE6Nv6Z7/u+BHDxdrXX3xutLRTuD7IqGifA9DcAk6XDtdyHiP7TwIAKoAouA6rRMaJQSTzN2lLjNIGPt9XAzasLf17Oxidbl/9S9+RBHPm7l47T08fOwhKz6f0w/ff0/sDYB8A0IgprtSgoECCjR8KFgwiZUqXwqgZKVK4MIXVTLHvMKxKZ1BgYAJcC7pMMevGAbSdxdv/N9aPyGJrAF0Gip48acJeAGbUOzOmpi3+aKHthQF9CbBmPcgDegTubq6ePLJffX/eu+qSjxbK786chhtuEOzBEHHrEkXgMicQwQYdW77/Rhcrkj1JxSacuIfDof27jPA6mr3xny9edOHVV0ZfhLGMBJPwOlwH9jYe8SSAYAGwpHudOrRLhZu3iY2gDu3bRWHl0Yb169E1q5bpN5qWEKFs2biO4ARjEQtMLLBqf4I5CiBW0O1pg8cUeI39/LeyuPgdeH2XTe5G1ifhDDz+Dq8vssnBgcFzooEwB+9RFGsWAsJc/31BQUG/g85wF0CZXLN6uKt1qxYycCmlZfOmSr06tWV4lEBzR/JUD6+mwOSpIMdJnVo1CXyO29coG7/5SsJmXDBGSszdqy7gjK5tWzd469WtTZZ/9nHcj5vXX5k4fqytb++eGfA7aRUrhKWCOXgM/v9PllhS5kkAwTKYsPuwcjMmjh9jO3fycDqYi9gJ3As2rDxtyiQFNFipaeOGyrgxo2RQxJRgkMUAHIx04aQ7gE6yG3qN9UAIY2lSedh/4GNpoAIW11CAETZ7fgHoRbReH/F9Y/1fE1iR9X7YsLbds927dgVADK9Vo/qSihXKfwTK6G7QIw4DSOMArA8A8KnArTJAH0kBDng9uFjRFOBsCnA4vZAHdA1auFAhOyiyrrDy5RTQQ+TQkBBcCMjd4oESGafD51+zcfJ/UvSBpiyXTV/JQARdyVivGBgYKONr5Kz4HoY4WQYMZsUcYit3HktS+U89irHJQiBjW/oGDNA1qlap1DYsrPwaUELPw7hcgvdOsRWO9AdbFK2wBJRxNRynzgyAT9zREmgW0F5APd4QsvGbQLvZzWxhWS7YX6c+o6eeJCQ/AUdejvPlHrlH7pF75B65R+6Re+QeuUfOH/8PoWEj8ufs3KMAAAAASUVORK5CYII="}}"""


        // Create Decoded Generic JSON - this just verifies the JSON is Valid
        assertDoesNotThrow { Json.parseToJsonElement(replyThumbnail) }

        // Create frame and 'Send'
        frameSender.send(Frame.Text("$channel:$replyThumbnail"))

        // Create a Decoded message from JSON and add channel
        val jsonVtMessage: ResultMessage =
            Json.decodeFromString(replyThumbnail)
        jsonVtMessage.channel = channel


        // Get decoded
        var lastReceive: Pair<Connection, ResultMessage>? = null
        while (lastReceive == null) {
            delay(1000)
            lastReceive = connectionListener.lastConnectionReceive
        }

        lastReceive.let { (_, resultMessage) ->
            println("Testing: $testName")

            assert(resultMessage is ResultMessage.ResultMessageWithPayload)

            assert(resultMessage.event == "payload")
            resultMessage as ResultMessage.ResultMessageWithPayload

            assert(resultMessage.type == "stateEvents")
            assert(resultMessage.id == "mini")
            assert(resultMessage.name == "avatar state")

            //Payload Check
            assert(resultMessage.payload is ResultPayload.ResultPayloadPng)
            val payload = resultMessage.payload as ResultPayload.ResultPayloadPng
            assert(payload.event == "thumb")
            assert(payload.state == "3")
            assert(payload.width == 129)
            assert(payload.height == 129)
            assert(
                payload.png == """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"""
            )

        }

    }


    @Test
    @Disabled("Mockk - Mocking Suspended functions is broken - https://github.com/mockk/mockk/issues/288")
    fun testSendReceive() {
        assertNotNull(conn) { "Connection is Null, Should never be the case" }

        conn?.let { conn ->
            assert(conn.isConnected) { "Connection not 'connected'" }

            mockWebSocketSession?.let { mockWebSocketSession ->

                //val contentSlot = slot<String>()
                val frameSlot = slot<Frame.Text>()


                //coEvery { mockWebSocketSession.send(any()) } just runs
                //coEvery { mockWebSocketSession.send(content = capture(contentSlot)) } just runs
                coEvery { mockWebSocketSession.send(frame = capture(frameSlot)) } just runs

                val request = VtRequest.getEventList
                val requestExpectedSend =
                    """nodes:{"event":"list"}""" //"""nodes:{"event":"payload","type":"stateEvents","id":"mini","payload":{"event":"list"}}"""
                val dataAsString = "nodes:${Json.encodeToString(RequestMessage.serializer(), request)}"

                println("$requestExpectedSend\n$dataAsString")

                assert(requestExpectedSend == dataAsString)

                coEvery { mockWebSocketSession.send(requestExpectedSend) } just runs
                coEvery { mockWebSocketSession.send(String()) } just runs
                coEvery { mockWebSocketSession.send(any()) } just runs

                runTest {
                    conn.send(requestData = request)

                    @OptIn(ExperimentalCoroutinesApi::class)
                    advanceUntilIdle()
                }

                //println("Capture: ${frameSlot.captured}")

                //coVerify {   mockWebSocketSession.send(Frame.Text(requestExpectedSend)) }

            }

        }

    }


    companion object {
        @JvmStatic
        var conn: Connection? = null

        @JvmStatic
        var mockWebSocketSession: WebSocketSession? = null

        @JvmStatic
        val dummyInstance = Instance(InstanceID("mini", 12345678, 1234), "dummy", "127.0.0.10:12345")

        @JvmStatic
        val frameSender = Channel<Frame>()

        @JvmStatic
        val frameReceive: ReceiveChannel<Frame> = frameSender

        @JvmStatic
        val connectionListener = testConnectionListener()

        @JvmStatic
        @OptIn(VisibleForUnitTests::class)
        @BeforeAll
        fun start() {

            mockWebSocketSession = mockk<WebSocketSession>()

            mockWebSocketSession?.let { mockWebSocketSession ->

                every { mockWebSocketSession.isActive } returns true

                conn = Connection(
                    instance = dummyInstance,
                    listener = connectionListener,
                    testFrameChannel = frameReceive,
                    mockWebSocketSession = mockWebSocketSession
                )
            }


        }
    }


}


class testConnectionListener : ConnectionListener {

    var lastConnectionError: Pair<Connection, ConnectionError>? = null
        private set
        // nulls field during return
        get() = field.apply { field = null }

    override fun onConnectionError(connection: Connection, error: ConnectionError) {
        println("onConnectionError: $connection ; $error")

        lastConnectionError = Pair(connection, error)
    }

    var lastConnectionChange: Pair<Connection, Boolean>? = null
        private set
        // nulls field during return
        get() = field.apply { field = null }

    override fun onConnectionChange(connection: Connection, active: Boolean) {
        println("onConnectionChange: $connection ; $active")

        lastConnectionChange = Pair(connection, active)
    }


    var lastConnectionReceive: Pair<Connection, ResultMessage>? = null
        private set
        // nulls field during return
        get() = field.apply { field = null }

    override fun onConnectionReceive(connection: Connection, message: ResultMessage) {
        println("onConnectionReceive: $connection ; $message")

        lastConnectionReceive = Pair(connection, message)
    }

}