package io.github.dissonantau.bleatkan


@Suppress("unused")
object MiscFunctions {

        /**
         * Returns current time as a Unix (Epoch) Timestamp,
         * the equivalent of the number of Seconds that have passed
         * since *1st January 1970 00:00:00 UTC*
         *
         * See the description of the class [java.util.Date] for
         * a discussion of slight discrepancies that may arise between
         * "computer time" and coordinated universal time (UTC).
         *
         * @return  Unix Timestamp - the difference, measured in seconds, between
         *          the current time and midnight, January 1, 1970 UTC.
         * @see     java.util.Date
         * @see     System.currentTimeMillis
         */
        @JvmStatic
        fun getUnixTime(): Long {
            return System.currentTimeMillis() / 1000L
        }

        /**
         * Converts C# Universal Time Ticks (Like from DateTime.ToUniversalTime().Ticks) to Unix Epoch Time
         */
        @JvmStatic
        fun convertFromUniversalTimeTicks(ticks:Long):Long{
            return (ticks - 621355968000000000L) / 10000000
        }

}

