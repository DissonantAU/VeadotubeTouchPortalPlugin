package io.github.dissonantau.bleatkan.instance


interface InstancesListener {

    /**
     * Instance Manager Event - New Instance Started/Detected
     */
    fun onInstanceStart(instance: Instance)

    /**
     * Instance Manager Event - Existing Instance Updated
     *
     * Usually a Name or Server IP change, requiring reconnection
     */
    fun onInstanceChange(instance: Instance, oldInstance: Instance)


    /**
     * Instance Manager Event - Existing Instance Closed
     */
    fun onInstanceEnd(id: InstanceID)
}
