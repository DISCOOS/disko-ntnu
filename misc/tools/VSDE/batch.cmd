# Connect current serial session 
set -param session -value serialsession
connect -port COM3 -baudrate 9600 -databits 5 -stopbits a -parity n -flowctrl n

# Connect current TNC session
set -param session -value tncsession
connect -port COM4 -tnctype KP-3 -baudrate 9600 -databits 8 -stopbits a -parity n -flowctrl n -hostmode kiss -inittnc y

# Connect to sessions directly
connect -session serialsession -port COM3 -baudrate 9600 -databits 5 -stopbits a -parity n -flowctrl n
connect -session tncsession -port COM4 -tnctype KP3 -baudrate 9600 -databits 8 -stopbits a -parity n -flowctrl n -hostmode kiss -inittnc y
