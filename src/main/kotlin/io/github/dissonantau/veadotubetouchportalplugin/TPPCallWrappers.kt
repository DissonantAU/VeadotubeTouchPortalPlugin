package io.github.dissonantau.veadotubetouchportalplugin

import com.christophecvb.touchportal.TouchPortalPlugin
import com.christophecvb.touchportal.model.TPNotificationOption

/**
 * Send a Choice Update Message to the Touch Portal Plugin System
 *
 * @param listId                String
 * @param values                String[]
 * @param allowEmptyArrayValues boolean
 * @return boolean choiceUpdateMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPChoiceUpdate(
    listId: String, values: Array<String>, allowEmptyArrayValues: Boolean = false
): Boolean = this.sendChoiceUpdate(listId, values, allowEmptyArrayValues)

/**
 * Send a Choice Update Message to the Touch Portal Plugin System
 *
 * @param listId                String
 * @param values                String[]
 * @param allowEmptyArrayValues boolean
 * @return boolean choiceUpdateMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPChoiceUpdate(
    listId: String, values: Collection<String>, allowEmptyArrayValues: Boolean = false
): Boolean = this.sendChoiceUpdate(listId, values.toTypedArray(), allowEmptyArrayValues)

/**
 * Send a Specific Choice Update Message to the Touch Portal Plugin System
 *
 * @param choiceId              String
 * @param instanceId            String
 * @param values                String[]
 * @param allowEmptyArrayValues boolean
 * @return boolean specificChoiceUpdateMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPSpecificChoiceUpdate(
    choiceId: String, instanceId: String, values: Array<String>, allowEmptyArrayValues: Boolean = false
): Boolean = this.sendSpecificChoiceUpdate(choiceId, instanceId, values, allowEmptyArrayValues)

/**
 * Send a Show Notification Message to the Touch Portal Plugin System
 *
 * @see [sendShowNotification]
 *
 * @param notificationId String
 * @param title          String
 * @param message        String
 * @param options        [com.christophecvb.touchportal.model.TPNotificationOption][]
 * @return boolean showNotificationMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPNotification(
    notificationId: String, title: String, message: String, options: Array<TPNotificationOption>
) = this.sendShowNotification(notificationId, title, message, options)

/**
 * Send a State Update Message to the Touch Portal Plugin System
 *
 * @see [sendStateUpdate]
 *
 * @param stateId         String (Full State ID)
 * @param value           Object
 * @param allowEmptyValue boolean
 * @param forceUpdate     boolean
 * @return boolean stateUpdateMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPStateUpdate(
    stateId: String, value: Any, allowEmptyValue: Boolean = false, forceUpdate: Boolean = false
): Boolean = this.sendStateUpdate(stateId, value, allowEmptyValue, forceUpdate)

/**
 * Send a Create a State Message to the Touch Portal Plugin System
 *
 * @see [sendCreateState]
 *
 * @param categoryId        String
 * @param stateId           String (Short State ID)
 * @param parentGroup       String
 * @param description       String
 * @param value             Object
 * @param allowEmptyValue   boolean
 * @param forceUpdate       boolean
 * @return boolean stateCreateSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPCreateState(
    categoryId: String, stateId: String, parentGroup: String? = null,
    description: String, value: Any, allowEmptyValue: Boolean = false, forceUpdate: Boolean = false
): Boolean = this.sendCreateState(categoryId, stateId, parentGroup, description, value, allowEmptyValue, forceUpdate)

/**
 * Send a Remove State Message to the Touch Portal Plugin System
 *
 * @see [sendRemoveState]
 *
 * @param categoryId String
 * @param stateId    String (Short State ID)
 * @return boolean removeStateSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPRemoveState(categoryId: String, stateId: String): Boolean =
    this.sendRemoveState(categoryId, stateId)


/**
 * Send a Setting Update Message to the Touch Portal Plugin System
 *
 * @see [sendSettingUpdate]
 *
 * @param settingName     String
 * @param value           String
 * @param allowEmptyValue boolean
 * @return boolean settingUpdateMessageSent
 */
@Suppress("NOTHING_TO_INLINE")
inline fun TouchPortalPlugin.sendTPSettingUpdate(
    settingName: String, value: String, allowEmptyValue: Boolean
): Boolean = this.sendSettingUpdate(settingName, value, allowEmptyValue)