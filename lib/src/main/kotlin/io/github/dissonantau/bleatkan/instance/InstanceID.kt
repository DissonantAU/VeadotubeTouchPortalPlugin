package io.github.dissonantau.bleatkan.instance


/**
 * Object to hold parse basic Instance information
 *
 * Immutable - can't be modified after creation
 *
 * For more information, refer to [the veadotube websocket documentation page](https://veado.tube/help/docs/websocket/#finding-the-server)
 *
 * Immutable, can't be modified after creation
 *
 * @see <a href="https://gitlab.com/veadotube/bleatcan/-/blob/b1d4faf70138c1e839b449c3cf799b6fd59c837b/bleatcan/InstanceID.cs">Veadotube bleatcan InstanceID.cs on Gitlab</a> (Original Reference)
 *
 */
class InstanceID : Comparable<InstanceID> {

    /**
     * Object to hold parse basic Instance information
     *
     * Immutable - can't be modified after creation
     *
     * The constructor Takes the File Name, Splits into 3 Parts and stores for reference, etc.
     * The String should resemble *mini-08dc8d3c583c0587-00000f38*
     *
     *  * Instance Type (*mini* for veadotube mini, *live* for veadotube live, or *editor* for veadotube editor for example)
     *  * Launch Timestamp (Unix Timestamp as a Hexadecimal Number, 16 characters)
     *  * Process ID (as Hexadecimal Number, 8 characters)
     *
     * For more information, refer to [the veadotube websocket documentation page](https://veado.tube/help/docs/websocket/#finding-the-server)
     *
     * @see <a href="https://gitlab.com/veadotube/bleatcan/-/blob/b1d4faf70138c1e839b449c3cf799b6fd59c837b/bleatcan/InstanceID.cs">Veadotube bleatcan InstanceID.cs on Gitlab</a> (Original Reference)
     *
     * @param type Veadotube Instance Type/Edition
     * @param timestamp Time Veadotube Instance was Launched
     * @param process Process ID of Veadotube Instance
     *
     */
    @Suppress("unused")
    constructor(type: String, timestamp: Long, process: Int) {
        require(timestamp > 0) { "Timestamp must be greater than 0" }
        require(process > 0) { "Process must be greater than 0" }
        require(type.isNotBlank()) { "Instance ID String must not be empty or blank" }

        this.type = type
        this.timestamp = timestamp
        this.process = process

        //Generate Instance ID String
        instanceID = generateInstanceIDString(type, timestamp, process)
    }


    /**
     * Object to hold parse basic Instance information
     *
     * Immutable - can't be modified after creation
     *
     * This constructor takes the File Name, Splits into 3 Parts and stores for reference, etc.
     * The String should resemble *mini-08dc8d3c583c0587-00000f38*
     *
     *  * Instance Type (*mini* for veadotube mini, *live* for veadotube live, or *editor* for veadotube editor for example)
     *  * Launch Timestamp (Unix Timestamp as a Hexadecimal Number, 16 characters)
     *  * Process ID (as Hexadecimal Number, 8 characters)
     *
     * For more information, refer to [the veadotube websocket documentation page](https://veado.tube/help/docs/websocket/#finding-the-server)
     *
     * @see <a href="https://gitlab.com/veadotube/bleatcan/-/blob/b1d4faf70138c1e839b449c3cf799b6fd59c837b/bleatcan/InstanceID.cs">Veadotube bleatcan InstanceID.cs on Gitlab</a> (Original Reference)
     *
     * @param instanceIdString Filename of Instance File
     *
     */
    constructor(instanceIdString: String) {

        require(instanceIdString.isNotBlank()) { "Instance ID String must not be empty or blank" }

        instanceID = instanceIdString.trim()
        val typeTemp: String?

        var timestampTemp = -1L
        var processTemp = -1


        //Split String
        val parts = instanceID.split("-")
        //Test result
        stringFormatCheck(parts)

        // Try to Convert Timestamp from Hex
        try {
            timestampTemp = parts[1].toLong(radix = 16) //Convert from Hex to Instant
        } catch (e: NumberFormatException) {
            require(false) { "Instance ID String Invalid: 2nd part is not valid Hex Value" }
        }


        // Try to Convert Process ID from Hex
        try {
            processTemp = parts[2].toInt(16) //Convert from Hex to int
        } catch (e: NumberFormatException) {
            require(false) { "Instance ID String Invalid: 3rd part is not valid Hex Value" }
        }


        typeTemp = parts[0]
        require(typeTemp.isNotEmpty() && typeTemp.all { it.isLetter() }) { "Instance ID String Invalid: 1st part contains non-letter characters" }


        //Assign Values
        this.type = typeTemp
        this.timestamp = timestampTemp
        this.process = processTemp

    }

    /**
     * Veadotube Instance Type/Edition
     *
     * e.g. mini, live, editor, etc.
     */
    val type: String

    /**
     * Time Veadotube Instance was Launched
     *
     * Converted from Hex, in C# DateTime Universal Time Ticks Format
     */
    val timestamp: Long

    /**
     * Process ID of Veadotube Instance
     *
     * Converted from Hex
     */
    val process: Int


    /** Store Filename/Original for use in toString*/
    private val instanceID: String


    /**
     * @return `String` in the format *Type-LaunchTime:Hex-ProcessId:Hex*
     */
    override fun toString(): String {
        return instanceID
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true //Same Object

        if (other !is InstanceID) return false // Not type InstanceID


        val that: InstanceID = other //Cast to type

        return timestamp == that.timestamp && process == that.process && type == that.type
    }


    /**
     * Comparison method for sorting, etc.
     * Sorted by Launch Time, then using ToString (Type-LaunchTime-ProcessId) if Launched at the same time
     *
     * @param other the InstanceID to be compared.
     * @return `int` representing whether the given InstanceID if before or after the first
     */
    override fun compareTo(other: InstanceID): Int {
        val c = this.timestamp.compareTo(other.timestamp)
        return if (c != 0) c else this.toString().compareTo(other.toString())
    }


    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + process
        return result
    }

    @Suppress("unused")
    companion object {
        /**
         * String Format - Converts back to same format as instance file name
         */
        const val FORMAT_STRING = "%s-%016x-%08x"

        /**
         * Generates an InstanceID String from an [InstanceID] using [FORMAT_STRING]
         */
        @JvmStatic
        fun generateStringFromID(instanceID: InstanceID) =
            FORMAT_STRING.format(instanceID.type, instanceID.timestamp, instanceID.process)


        /**
         * Generates an InstanceID String using [FORMAT_STRING]
         *
         * @param type Veadotube Instance Type/Edition
         * @param timestamp Time Veadotube Instance was Launched
         * @param process Process ID of Veadotube Instance
         */
        @JvmStatic
        fun generateInstanceIDString(type: String, timestamp: Long, process: Int) =
            FORMAT_STRING.format(type, timestamp, process)


        /**
         * Takes the File Name, Splits into 3 Parts Tests
         * The String should resemble *mini-08dc8d3c583c0587-00000f38*
         *
         *  * Instance Type (*mini* for veadotube mini, *live* for veadotube live, or *editor* for veadotube editor for example)
         *  * Launch Timestamp (Unix Timestamp as a Hexadecimal Number, 16 characters)
         *  * Process ID (as Hexadecimal Number, 8 characters)
         *
         *  @throws IllegalArgumentException
         */
        @JvmStatic
        fun stringFormatCheck(instanceIdString: String) {
            val parts = instanceIdString.trim().split("-")
            stringFormatCheck(parts)
        }

        /**
         * Takes pre-split File Name - Should be 3 parts
         * The String should resemble *mini-08dc8d3c583c0587-00000f38*
         *
         *  * Instance Type (*mini* for veadotube mini, *live* for veadotube live, or *editor* for veadotube editor for example)
         *  * Launch Timestamp (Unix Timestamp as a Hexadecimal Number, 16 characters)
         *  * Process ID (as Hexadecimal Number, 8 characters)
         *
         *  @throws IllegalArgumentException
         */
        @JvmStatic
        private fun stringFormatCheck(instanceIdParts: List<String>) {
            //Check # of parts
            require(instanceIdParts.size == 3) { "Instance ID String Invalid: Must be 3 parts, separated by dashes (-)" }

            //Check Part lengths are correct
            require(instanceIdParts[1].length == 16) { "Instance ID String Invalid: 2nd part must be 16 Characters long" }
            require(instanceIdParts[2].length == 8) { "Instance ID String Invalid: 3nd part must be 8 Characters long" }
            require(instanceIdParts[0].isNotBlank()) { "Instance ID String Invalid: 1nd part must not be blank" }

        }

        @JvmStatic
        fun equals(a: InstanceID, b: InstanceID): Boolean {
            return a == b
        }

        @JvmStatic
        fun notEquals(a: InstanceID, b: InstanceID): Boolean {
            return a != b
        }


    }
}
