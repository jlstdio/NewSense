#include <Adafruit_NeoPixel.h>
#include <RFduinoBLE.h>

#define sonar 8
#define neo 10

Adafruit_NeoPixel strip = Adafruit_NeoPixel(16, neo, NEO_GRB + NEO_KHZ800);

long dis_pwm/*센서 pwm 값*/, dis/*거리 계산 값*/;

void light_off() // 라이트 끄기
{
  for(int i=0;i<=15;i++) strip.setPixelColor(i,0,0,0);
}

void sensing() // 거리 센싱
{
  dis_pwm = pulseIn(sonar,HIGH);
  dis = dis_pwm/58;
  RFduinoBLE.sendInt(dis);
}

void light(int dis) // 파란색 주기적인 led
{
  int i;
  for(i=0;i<=15;i++) strip.setPixelColor(i,0,0,255);
  delay((dis/2000 +1) * 1000); // 10000cm 5s / 8000cm 4s / 6000cm 3s / 4000cm 2s / 2000cm /1s
  for(i=0;i<=15;i++) strip.setPixelColor(i,0,0,0);
  delay(1000);
}

void turn(char stat) // 턴시그널 함수
{
  int i;
  if(stat == 'r')
  {
    for(i=0;i<=15;i++)
    {
      strip.setPixelColor(i,255,165,0);
      delay(100);
    }
  }
  if(stat == 'l')
  {
    for(i=15;i>=0;i--)
    {
      strip.setPixelColor(i,255,165,0);
      delay(100);
    }
  }
  for(i=0;i<=15;i++) strip.setPixelColor(i,0,0,0);
  delay(500);
}

void brk() // 브레이크 라이트 함수
{
  for(int i=0;i<=15;i++) strip.setPixelColor(i,255,0,0);
}

void RFduinoBLE_onReceive(char *data,int len)
{
  uint8_t stat = data[0];
  if(stat == 'r') turn('r');
  if(stat == 'l') turn('l');
  if(stat == 'b') brk();
}

void setup() {
  RFduinoBLE.deviceName = "NewSense";
  RFduinoBLE.begin();
  pinMode(sonar,INPUT);
  pinMode(neo,OUTPUT);
}

void loop() {
  sensing();
  light_off(); // loop 돌때 마다 라이트 끄고 시작
  light(dis);
}
