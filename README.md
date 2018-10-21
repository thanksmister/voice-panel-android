# Voice Panel

Voice Panel is an Android Voice Assistant for [Home Assistant](https://www.home-assistant.io/) powered by the [Snips Voice Playtform](https://snips.ai/). Snips provides a private, powerful, and customizable voice assistant technology that processes all language input on the device, nothing is ever sent to the cloud.

Voice Panel uses Snips to act as a voice interface for Home Assistant. At this time, you can control your alarm system, lights, windows, blinds, switches, check status, get the date/time, and retrieve the weather information. You initiate a conversation with Voice Panel by using speaking wake-word, "Hey, Snips".  Alternatively, you can use face detection to initiate a conversation simply by looking at the device. 

Currently the application has a few limitations.  The Snips Android SDK does not work as a satelite, all messages must be forworded to Home Assistant using MQTT and speach is processed on the Android device using TTS.  The Snips Android SDK does not support custom wake-words at this time.  To initiate a conversation, you must say "Hey, Snips" or you can try the face detection wake-word feature which starts listening when the camera recognizes a face. 

## Support

For issues, feature requests, comments or questions, use the [Github issues tracker](https://github.com/thanksmister/vioce-panel-android/issues).  

## Features
- Face activated wake-word (no need to say "Hey, Snips").
- Control Home Assistant components using voice commands ("Turn on the kitchen lights").
- Stream video, detect motion, detect faces, and read QR Codes.
- Suport for MQTT Alarm Panel Control to control your alarm system.
- MQTT commands to remotely control the application (speak text, play audio, send notifications, alerts, etc.).
- Device sensor data reporting over MQTT (temperature, light, pressure, battery, etc.).
- MQTT Day/Night mode based on the sun value reported from Home Assistant.
- MQTT weather data to display weather reported from Home Assistant.

## Screen Shots:

![ui](https://user-images.githubusercontent.com/142340/47121221-42808f80-d248-11e8-813a-72b3be85eae3.png)
![landscape](https://user-images.githubusercontent.com/142340/47121143-e3227f80-d247-11e8-979a-3bdf19ceb998.png)

## Hardware & Software 

- Android Device running Android OS 5.0.1 (SDK 21) or greater. Though performance on older devices may vary. Google Play services may also be required, this may affect some Amazon Fire Tablet devices. 

### Installation

You can download and install the latest release from the [release section](https://github.com/thanksmister/voice-panel-android/releases). 

## Assistant Setup

You first need to setup a [MQTT Broker](https://www.home-assistant.io/addons/)in Home Assistant by adding the HASS MQTT Broker add-on to Home Assistant.  This allows the two-way communication between Home Assistant and Voice Panel using the MQTT messaging protocal. 

For the Voice Assistant to control Home Assistant components, you neeed to install either the [Snips component](https://www.home-assistant.io/components/snips/) or the [Snips Add-On](https://www.home-assistant.io/addons/snips/). 

Home Assistant already has [bundled scripts](https://developers.home-assistant.io/docs/en/intent_builtin.html) included when you add the Snips platform to Home Assistant.  These scripts will allow you to turn on/off components (lights, switches, etc.), open/close components (garage, blinds, etc.), set the color of lights, add items to your shopping list, and retrieve items from your shopping list.   So you can say "Turn on the kitchen lights" to control a the component lights.kitchen. 

### Custom Assistant Commands

Voice Panel also has custom scripts that extend upon the basic functionality. Currently Voice Panel can control your the MQTT alarm panel in Home Assistant, get the status for any component by name ("What's the status of the front door?"), get the time/date, get the weather, and control the thermostate.  

To use custom scripts you need to create a file to store your [intent scripts](https://www.home-assistant.io/components/intent_script/) and customize the assistants behavior.  Create an "intents.yaml" file in your confi directory, then link this from the configuration.yaml file by adding this line at the bottom: 

```intent_script: !include intents.yaml```

You will place intents scripts within the new "intents.yaml" file to work with the various components currently supported by application.  Home Assistant already has a [bundled scripts](https://github.com/tschmidty69/hass-snips-bundle-intents) included when you add the Snips platform to Home Assistant.  Voice Panel also has custom scripts to extend upon the basic functionality.  The additional scripts can be added to your intents yaml file to use these features. 


### Alarm Panel Control

To control a Home Assistant alarm control panel using voice, you need to add the alarm intents scripts to your "intents.yaml" file. This will allow you to set the alarm status, disarm the alarm, and set the alarm to home or away modes by voice.  Here are sample voice commands: 

  * "Set the alarm to home"
  * "Disable the alarm"
  * "Set the alarm to away mode"
  * "What's the status of the alarm?"

#### Alarm Panel Intents

```
HaAlarmStatus:
  speech:
    type: plain
    text: >
      {% if is_state("alarm_control_panel.ha_alarm", "disarmed") -%}
        The alarm is not active.
      {%- endif %}
      {% if is_state("alarm_control_panel.ha_alarm", "armed_home") -%}
        The alarm is in home mode.
      {%- endif %}
      {% if is_state("alarm_control_panel.ha_alarm", "armed_away") -%}
        The alarm is in away mode.
      {%- endif %}
HaAlarmHome:
  speech:
    type: plain
    text: > 
      OK, alarm set to home.
  action:
    - service: alarm_control_panel.alarm_arm_home
      data:
        entity_id: alarm_control_panel.ha_alarm
HaAlarmAway:
  speech:
    type: plain
    text: >
      OK, the alarm set to away, you have 60 seconds to leave.
  action:
    - service: alarm_control_panel.alarm_arm_away
      data:
        entity_id: alarm_control_panel.ha_alarm
HaAlarmDisarm:
  speech:
    type: plain
    text: >
      The alarm has been deactivated.
  action:
    - service: alarm_control_panel.alarm_disarm
      data:
        entity_id: alarm_control_panel.ha_alarm
```

### Weather Updates 

If you would like to get weather updates, you will need to add a [weather component](https://www.home-assistant.io/components/sensor.darksky/) to Home Assistant. Voice Panel can report the weather by adding a new intent that will speak the current weather conditions to your "intents.yaml" file.  Here are sample voice commands: 

  * "What's the weather today?"

#### Example Darksky Weather 

```
searchWeatherForecast:
  speech:
    type: plain
    text: >
      The weather is currently 
      {{states('sensor.dark_sky_temperature') | round(0)}} 
      degrees outside and 
      {{states('sensor.dark_sky_daily_summary')}} 
      The high today will be 
      {{ states('sensor.dark_sky_daytime_high_temperature') | round(0)}}
      and 
      {{ states('sensor.dark_sky_hourly_summary')}}
```

### Status Updates 

You can get the status of Home Assistant components using voice commands.  First you need to add the intent scripts to the "intents.yaml" file to handle script information.   The status intent has two slots, one for the entity location (such as kitchen, living room, deck, upstairs, etc.) and one for entity id (door, camera, light, etc.).   These slots can be used to provide a specific response for the item status.   

* "What's the staus of the main door"
* "Main door state?"
* "Are the doors secure?"

#### Example Status Intent Script

``` 
getStatus:
  speech:
    type: plain
    text: >
      {% if entity_locale == 'main' and  entity_id == 'door'  %}
        The {{ entity_locale }} {{ entity_id }} is {{states(sensor.main_door)}}
      {% endif %} 
 ```
 
In the above example, "entity_locale" and "entity_id" are used to provide a sepcific sensor status.  In this case we asked "what is the status of the main door?" and Snips responsed with "The main door is closed", which is the status of the sensor.main_door entity.  

### Time/Date 

You can get the current time and date from Home Assistant components using voice commands.  Add the follwing intent scripts to the "intents.yaml" file to handle intents for Time/Date.  

* "What's the date?"
* "What day is it?"
* "What time is it?"

#### Example Status Intent Script
```
getCurrentTime:
  speech:
    type: plain
    text: > 
      The time is {{ now().hour}}  {{ "%0.02d" | format(now().strftime("%-M") | int) }}
getCurrentDate:
  speech:
    type: plain
    text: > 
      It is {{ now().weekday }}, the {{ now().day }} of {{ now().month }}, {{ now().year }}
getCurrentDay:
  speech:
    type: plain
    text: > 
      Today is { {now().weekday }}
```

## MQTT Communication

In addition to voice commands, the Voice Panel application can display and control components using the MQTT protocal.  Voice Panel and Home Assistant work together to control the Home Assistant Alarm Control Panel, display weather data, receive sensor data, control the application Day/Night mode, and send various remote commands to the application. 

### MQTT Alarm Panel Control

![alarm](https://user-images.githubusercontent.com/142340/47173519-a5276900-d2e4-11e8-84e9-db623b461020.png)

The alarm panel can be controlled using only voice, however included is a manual way to set and disable the alarm which works using MQTT messaging.  To use this feature, you need to install the [Manual Alarm Control Panel with MQTT Support](https://www.home-assistant.io/components/alarm_control_panel.manual_mqtt/).  This component allows for two-way control of the Home Assistant alarm panel component using MQTT messaging.

To enable the MQTT alarm feature, under settings (the gear icon) select Alarm Settings. Once active, you will see a lock icon at the bottom of the main screen which displays the current alarm mode and provide a manual means for arming and disarming the alarm in addition to the voice controls.  The Alarm Settings has options to change the MQTT topic and commands if you do not wish to use the defaults. 

#### Supported Command and Publish States

- Command topic:  home/alarm/set
- Command payloads: ARM_HOME, ARM_AWAY, DISARM
- Publish topic: home/alarm
- Publish payloads: disarmed, armed_away, armed_home, pending, triggered (armed_night not currently supported).

#### Example Home Assistant Setup

```
alarm_control_panel:
  - platform: manual_mqtt
    state_topic: home/alarm
    command_topic: home/alarm/set
    pending_time: 60
    trigger_time: 1800
    disarm_after_trigger: false
    delay_time: 30
    armed_home:
      pending_time: 0
      delay_time: 0
    armed_away:
      pending_time: 60
      delay_time: 30
```

-- If I set the the alarm mode home, the alarm will immediately be on without any pending time.  If the alarm is triggered,      there will be no pending time before the siren sounds.   If the alarm mode is away, I have 60 seconds to leave before the      alarm is active and 30 seconds to disarm the alarm when entering.   

-- Notice that my trigger_time is 1800 and disarm_after_trigger is false, this means the alarm runs for 1800 seconds until it    stops and it doesn't reset after its triggerd. 


### MQTT Weather

![weather](https://user-images.githubusercontent.com/142340/47173511-a193e200-d2e4-11e8-8cbc-f2d57cdb6346.png)

You can also use MQTT to publish the weather to the Voice Panel application, which it will then display on the main view. To do this you need to setup an automation that publishes a formatted MQTT message on an interval.  Then in the application settings, enable the weather feature. Here is a sample automation that uses Darksky data to publish an MQTT message: 

```
- id: '1538595661244'
  alias: MQTT Weather
  trigger:
  - minutes: /5
    platform: time
  condition: []
  action:
  - data:
      payload_template: '{''weather'':{''summary'':''{{states(''sensor.dark_sky_summary'')}}'',''precipitation'':''{{states(''sensor.dark_sky_precip_probability'')}}'',''icon'':''{{states(''sensor.dark_sky_icon'')}}'',''temperature'':''{{states(''sensor.dark_sky_apparent_temperature'')}}'',''units'':''{{states.sensor.dark_sky_apparent_temperature.attributes.unit_of_measurement}}''}}'
      topic: voicepanel/command
      retain: true
    service: mqtt.publish
```

The resulting payload will look like this:

```
{"topic": "voicepanel/command","payload":"{'weather':{'summary':'Partly Cloudy','precipitation':'0','icon':'partly-cloudy-day','temperature':'22.5','units':'°C'}}
```

### MQTT Day/Night Mode

Similar to how weather works, you can control the Voice Panel to display the day or night mode by sending a formatted MQTT message with the sun's position (above or below the horizon).  To do this add the [sun component](https://www.home-assistant.io/components/sun/) to Home Assistant, then setup an automation to publish an MQTT message on an interval:

```
- id: '1539017708085'
  alias: MQTT Sun
  trigger:
  - minutes: /5
    platform: time
  condition: []
  action:
  - data:
      payload_template: '{''sun'':''{{states(''sun.sun'')}}''}'
      retain: true
      topic: voicepanel/command
    service: mqtt.publish
```

The resulting payload will look like this:

```
{"topic": "voicepanel/command","payload":"{'sun':'above_horizon'}}
```

You can also test this from the using the "mqtt.publish" service under the Home Assistant Developer Tools:

```
{
  "payload_template": "{'sun':'{{states('sun.sun') }}'}",
  "topic": "voicepanel/command"
}
```

If you wish, you can use an offset to change the day or night mode values or send a MQTT message at the desired time with "above_horizon" to show day mode or "below_horizon" to show night mode.  If you wish to always be night, you need only send one MQTT message with "below_horizon" and the app will not switch back to day mode.  Be sure to turn on the Day/Night mode under the Display settings in the application.  


### MQTT Sensor and State Data
If MQTT is properly configured, the application can publish data and states for various device sensors, camera detections, and application states. Each device required a unique base topic which you set in the MQTT settings, the default is "voicepanel".  This distinguishes your device if you are running multiple devices.  

#### Device Sensors
The application will post device sensors data per the API description and Sensor Reading Frequency. Currently device sensors for Pressure, Temperature, Light, and Battery Level are published. 

#### Sensor Data
Sensor | Keys | Example | Notes
-|-|-|-
battery | unit, value, charging, acPlugged, usbPlugged | ```{"unit":"%", "value":"39", "acPlugged":false, "usbPlugged":true, "charging":true}``` |
light | unit, value | ```{"unit":"lx", "value":"920"}``` |
magneticField | unit, value | ```{"unit":"uT", "value":"-1780.699951171875"}``` |
pressure | unit, value | ```{"unit":"hPa", "value":"1011.584716796875"}``` |
temperature | unit, value | ```{"unit":"°C", "value":"24"}``` |

*NOTE:* Sensor values are device specific. Not all devices will publish all sensor values.

* Sensor values are constructued as JSON per the above table
* For MQTT
  * WallPanel publishes all sensors to MQTT under ```[voicepanel]/sensor```
  * Each sensor publishes to a subtopic based on the type of sensor
    * Example: ```voicepanel/sensor/battery```
    
#### Home Assistant Examples
```YAML
sensor:
  - platform: mqtt
    state_topic: "voicepanel/sensor/battery"
    name: "Alarm Panel Battery Level"
    unit_of_measurement: "%"
    value_template: '{{ value_json.value }}'
    
 - platform: mqtt
    state_topic: "voicepanel/sensor/temperature"
    name: "WallPanel Temperature"
    unit_of_measurement: "°C"
    value_template: '{{ value_json.value }}'

  - platform: mqtt
    state_topic: "voicepanel/sensor/light"
    name: "Alarm Panel Light Level"
    unit_of_measurement: "lx"
    value_template: '{{ value_json.value }}'
    
  - platform: mqtt
    state_topic: "voicepanel/sensor/magneticField"
    name: "Alarm Panel Magnetic Field"
    unit_of_measurement: "uT"
    value_template: '{{ value_json.value }}'

  - platform: mqtt
    state_topic: "voicepanel/sensor/pressure"
    name: "Alarm Panel Pressure"
    unit_of_measurement: "hPa"
    value_template: '{{ value_json.value }}'
```

### Camera Motion, Face, and QR Codes Detections
In additional to device sensor data publishing. The application can also publish states for Motion detection and Face detection, as well as the data from QR Codes derived from the device camera.  

Detection | Keys | Example | Notes
-|-|-|-
motion | value | ```{"value": false}``` | Published immediately when motion detected
face | value | ```{"value": false}``` | Published immediately when face detected
qrcode | value | ```{"value": data}``` | Published immediately when QR Code scanned

* MQTT
  * WallPanel publishes all sensors to MQTT under ```[voicepanel]/sensor```
  * Each sensor publishes to a subtopic based on the type of sensor
    * Example: ```voicepanel/sensor/motion```

#### Home Assistant Examples

```YAML
binary_sensor:
  - platform: mqtt
    state_topic: "voicepanel/sensor/motion"
    name: "Motion"
    payload_on: '{"value":true}'
    payload_off: '{"value":false}'
    device_class: motion 
    
binary_sensor:
  - platform: mqtt
    state_topic: "voicepanel/sensor/face"
    name: "Face Detected"
    payload_on: '{"value":true}'
    payload_off: '{"value":false}'
    device_class: motion 
  
sensor:
  - platform: mqtt
    state_topic: "voicepanel/sensor/qrcode"
    name: "QR Code"
    value_template: '{{ value_json.value }}'
    
```

### Application State Data
The application canl also publish state data about the application such as the current dashboard url loaded or the screen state.

Key | Value | Example | Description
-|-|-|-
currentUrl | URL String | ```{"currentUrl":"http://hasbian:8123/states"}``` | Current URL the Dashboard is displaying
screenOn | true/false | ```{"screenOn":true}``` | If the screen is currently on

* State values are presented together as a JSON block
  * eg, ```{"currentUrl":"http://hasbian:8123/states","screenOn":true}```
* MQTT
  * WallPanel publishes state to topic ```[voicepanel]/state```
    * Default Topic: ```voicepanel/state```

## MQTT Commands
Interact and control the application and device remotely using either MQTT commands, including using your device as an announcer with Google Text-To-Speach. Each device required a unique base topic which you set in the MQTT settings, the default is "voicepanel".  This distinguishes your device if you are running multiple devices.  

### Commands
Key | Value | Example Payload | Description
-|-|-|-
audio | URL | ```{"audio": "http://<url>"}``` | Play the audio specified by the URL immediately
wake | true | ```{"wake": true}``` | Wakes the screen if it is asleep
speak | data | ```{"speak": "Hello!"}``` | Uses the devices TTS to speak the message
alert | data | ```{"alert": "Hello!"}``` | Displays an alert dialog within the application
notification | data | ```{"notification": "Hello!"}``` | Displays a system notification on the devie

* The base topic value (default is "voicepanel") should be unique to each device running the application unless you want all devices to receive the same command. The base topic and can be changed in the application settingssettings.
* Commands are constructed via valid JSON. It is possible to string multiple commands together:
  * eg, ```{"clearCache":true, "relaunch":true}```
* MQTT
  * WallPanel subscribes to topic ```[voicepanel]/command```
    * Default Topic: ```voicepanel/command```
  * Publish a JSON payload to this topic (be mindfula of quotes in JSON should be single quotes not double)

### Google Text-To-Speach Command
You can send a command using either HTTP or MQTT to have the device speak a message using Google's Text-To-Speach. Note that the device must be running Android Lollipop or above. 

Example format for the message topic and payload: 

```{"topic":"voicepanel/command", "payload":"{'speak':'Hello!'}"}```

## MJPEG Video Streaming

Use the device camera as a live MJPEG stream. Just connect to the stream using the device IP address and end point. Be sure to turn on the camera streaming options in the settings and set the number of allowed streams and HTTP port number. Note that performance depends upon your device (older devices will be slow).

#### Browser Example:

```http://192.168.1.1:2971/camera/stream```

#### Home Assistant Example:

```YAML
camera:
  - platform: mjpeg
    mjpeg_url: http://192.168.1.1:2971/camera/stream
    name: Voice Panel Camera
```

