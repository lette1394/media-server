# Table of Contents


## About
객체지향과 클린 아키텍처에서 강조하는 원칙들과 DDD 패턴을 가지고 실제 비즈니스 요구사항들을 구현한다.
이 프로젝트는 단순히 end-user 서비스 요구사항이 아닌, 보다 플랫폼의 측면에서 요구사항을 정리하고 구현한다


### Principles 원칙



## Domain: 이 소프트웨어는 무엇을 해결하려 하는가?
짧은 답변: 서비스 개발에는 "다양한" 기능이 있는 네트워크 파일 스토리지가 필요하다

긴 답변: |
서비스 개발을 하다보면 이미지를 사용할 일은 많다. 동영상은 이미지보다는 아니지만 요즘에는 꽤 고해상도의 영상까지 자주 사용되고 있다. 뿐만 아니라 어떤 사용자는 ms excel 파일이나 log 같은 텍스트만을, 어떤 팀은 zip와 같은 압축파일 바이너리를 어딘가에 두고 언제든지 사용하고 싶어한다. 그러나 이러한 파일들을 어디에 저장하고 또 어떻게 서비스에 사용한단 말인가? 서비스에서는 비즈니스 로직이 중요하므로, 필요한 파일을 읽고 쓰는데는 최소한의 오버헤드만 발생해야 하고, 또 이를 사용하는 방법도 매우 단순하길 원할 것이다. 

이 소프트웨어는 이러한 요구사항에 대한 궁극적인 답변이자 서비스에서 원하는 형태의 소프트웨어이다. 빠르며, 이해하기 쉽고, 미디어 파일에 대해 다양한 기능을 지원하며, 새로운 기능을 추가하고 확장하기 용이한 형태의 구조를 가지고 있다. 단순히 이진 바이너리를 저장하는 네트워크 스토리지 시스템에서부터, 이미지를 자르고 비디오를 인코딩하고 파일 포맷을 변경하고 접근 빈도에 가장 적합한 네트워크 스토리지를 자동으로 적용하고 미디어를 분석하여 서비스에서 손쉽게 결과를 가져다 사용할 수 있는 통 미디어 프로세싱 스토리지까지 여러 모습을 두루 갖추고 있는 완성형 소프트웨어가 서비스 팀에게 필요하다. 


## Domain description (TODO: write in english)
클라이언트는 스토리지 내부 스페이스에 파일을 업로드 할 수 있다. 파일은 업로드가 완료된 이후 스페이스, 파일 키, 파일 크기, 파일 종류(text/image/video/audio/binary), 업로드 시간을 알고 있어야 한다. 이를 파일의 메타 정보라고 부른다. 파일은 네트워크 문제 혹은 성능 개선을 위해 여러번에 걸쳐서 업로드 될 수 있다. 업로드가 예상치 못하게 중단되는 경우 클라이언트는 마지막으로 파일이 업로드 된 부분부터 업로드를 이어서 할 수 있다. 클라이언트는 스페이스 구분 없이 마구잡이로 업로드를 할 수 없으며, 요청 파일의 메타 정보 또는 요청에 같이 포함된 추가 정보를 기반으로 스페이스 관리자가 업로드 여부를 최종 결정할 수 있다. 거부된 파일은 스페이스에 저장되지 않으며 업로드 된 데이터는 단순히 버려진다. 스페이스는 같은 파일에 대한 업로드가 동시에 들어오는 경우, 어떤 요청을 수행할 것인지 선택할 수 있으며, 스페이스마다 선택하는 방식은 각각 다를 수 있다. 

미디어 파일은 업로드 완료 이후에 후처리를 통해 인코딩되어 복수개의 파생 파일이 생길 수 있다. (원본 파일 + 인코딩 파일), (인코딩 파일), (원본 파일 + 인코딩 파일1 + 인코딩 파일2 + ...) 어떤 방식으로 인코딩 할지는 스페이스에서 결정하거나 스페이스 관리자가 개별 파일 연산마다 결정할 수 있다. 파일의 후처리가 완료된 이후에 원본에 대한 삭제를 수행하거나 일정 기간 이후에 삭제할 수 있다. 이 동작 역시 스페이스 관리자가 개별적으로 결정하거나 스페이스에서 일괄적으로 처리할 수 있다. 클라이언트는 후처리 시작 전에 완료 응답을 받으며, 이후 후처리 결과는 스페이스 관리자에게 전달된다.  

클라이언트는 다른 스페이스에서 내 스페이스로 파일을 복사해 올 수 있으며 이 때 다른 스페이스가 요청을 거부하는 경우 요청은 취소된다. 스페이스와 파일 구성에 따라 실제로 데이터를 복사하지 않고 단순히 소스 파일에 대한 참조만 가지고 마치 복사가 수행된 듯 전체 복사 연산을 완료할 수 있다. 스토리지 성능 최적화를 위해 특정 참조 횟수가 기준치를 넘는 경우 새로운 복사 대상이 생성되며, 이후 참조 복사는 새롭게 생성된 복사 대상을 바라본다. 

스페이스 관리자(administrator)는 클라이언트의 파일 연산에 대해 개입할 권한이 있다. 파일 연산을 허용/거절하는 이유는 관리자마다, 파일마다 각각 다르다. 일반적으로 공개 네트워크에 위치한 클라이언트가 개인정보가 포함된 파일에 대해 접근하려고 할 때 관리자는 이를 제한할 수 있다. 또한 관리자는 자신이 관리하는 스페이스에 어떤 일이 일어나는지에 대한 알람을 받을 수 있다. (미디어 파일에 대한 인코딩 완료 알림, 다수의 다운로드 에러 알림 등) 파일 연산 중에 관리자는 클라이언트에게 추가적인 정보를 주거나 클라이언트로부터 정보를 받아서 파일 연산에 필요한 데이터를 스토리지에 알려줄 수 있다.   
 
빠른 다운로드를 위해 스토리지는 캐시된 파일을 내려줄 수 있다. 클라이언트는 기존 파일을 덮어쓸 때 캐시된 파일에 대한 업데이트 여부를 결정할 수 있다. 

클라이언트는 업로드한 파일을 삭제할 수 있다. 파일은 바로 삭제되거나, 생성한지 X일 후에 삭제되거나, 마지막 접근 Y일 이후에 삭제될 수 있다. 

스페이스는 드물게 사용되는 파일에 대해 효율적인 하드웨어 사용을 위해 접근 성능은 느리지만 대용량 저장소에 그 파일을 자동으로 옮길 수 있다. 사용 빈도가 기준치를 넘으면 다시 고성능의 접근 성능을 가진 물리 하드웨어 위치로 파일을 다시 옮길 수 있다. 스페이스 관리자는 이 과정을 거부하거나 기준을 변경할 수 있으나, 사용하는 스페이스에 따라 스토리지가 관리자의 요청을 거부할 수 있다.  


자동화된 설정 관리 //




### 클라이언트란? 
파일 연산을 요청하는 주체이다. 클라이언트는 파일에 대한 업로드, 다운로드, 삭제, 인코딩, 복사 등 파일에 대한 모든 연산을 수행 할 수 있다. 스페이스는 클라이언트의 네트워크 위치에 따라 퍼블릭, 인터널, 시스템 클라이언트로 구분할 수 있다. 스페이스 혹은 스페이스 관리자는 클라이언트의 종류에 따라 클라이언트의 일부 연산을 의도적으로 제한할 수 있다. 퍼블릭과 인터널 클라이언트는 네트워크 위치로 구분하며, 스토리지가 위치한 네트워크에 같이 있으면 인터널, 퍼블릭 네트워크에 위치해 있으면 퍼블릭 클라이언트이다. 시스템 클라이언트는 네트워크 위치와 관계없이 스토리지를 운영에 필요한 클라이언트를 가리킨다. 디버깅 용도로 로컬 네트워크 혹은 캐시를 위한 엣지 네트워트 모듈이 시스템 클라이언트에 해당한다. 

### 스페이스 관리자란(administrator)?
스페이스 관리자는 스페이스를 관리하는 운영자로서, 자신에게 속한 모든 스페이스와 파일들, 그리고 유저를 관리한다. 

### 파일 연산이란?
좁은 의미로는 스토리지가 제공하는 기능들이며, 넓은 의미로는 이 기능들을 조합하여 더 복잡한 기능을 구성하는 것까지 포함한다.   

### 제한이란? 
그 의도나 연산 가능성에 상관 없이 파일 연산에 대해 강제적인 금지 혹은 허용을 결정할 수 있는 복수개의 정책들이다. 스페이스마다 각자의 제한을 두고 있으며 스토리지 전체에 적용되는 제한 역시 존재한다.   
 
### 스토리지란?
스페이스들의 집합 및 이에 대한 연산이다. 스토리지는 여러 스페이스로 구성되며, 스페이스가 관리하고 있는 파일의 개수나 크기와 관계 없이 스토리지는 논리적인 단위로 스페이스를 관리할 수 있다. 스페이스 단위의 연산이 존재할 수 있다.   

### 스페이스란?
파일들의 집합 및 이에 대한 연산이다. 스페이스는 여러 파일로 구성되며, 파일의 크기나 종류에 상관없이 스페이스는 논리적인 단위로 파일을 다룬다. 다른 스페이스에 있는 파일 역시 연산의 일부로서 간주할 수 있다.  

### 파일이란?
논리적인 연산의 최소 단위. 파일은 크기, 생성 시각 등 본질적인 속성을 가지고 있으며 일부 특별한 파일은 자신만의 전용 연산을 가지고 있다. 바이너리 파일은 가장 단순한 형태의 파일로서, 기본적인 생성/삭제의 연산을 가지고 있다. 메타 파일은 바이너리 파일에 대한 메타 정보를 가지고 있다. 사용자는 단순히 메타 정보 조회를 위해 이 파일을 열어볼 수 있다. 미디어 파일은 이미지, 비디오 등 그 종류에 따라 종류가 다시 나뉘며, 썸네일 추출이나 비디오 인코딩 같은 미디어 전용 연산을 수행할 수 있다. 복사 파일은 디스크 사용률을 낮추기 위해 단순히 원본에 대한 참조를 가지고 있는 형태이거나, 디스크 접근 성능을 높이기 위해 물리적으로 같은 내용을 여러군데 복사할 수 있다. 캐시 파일은 위에서 언급한 다른 어떤 파일도 될 수 있으며 성능을 위해 압축같은 추가적인 연산을 수행할 수 있다. 삭제된 파일은 얼핏 말이 되지 않으나 논리적으로는 삭제되었으나 물리적으로 삭제가 되지 않았거나, 삭제를 나중에 수행하는 연산 등에 사용될 수 있다.






(문제 영역, 해결 영역)
당신은 대규모의 IT 기업에서 근무 중이다. 모든 네트워크 발생은 지출로 이어지며, 반대로 네트워크 사용량을 줄일 수록 비용은 줄어든다








### Requirements 1
- [storage] Support streaming: 10GB file up/download
  - [x] implementation    
  - [x] support tag / metadata
  - [x] object policy
- [storage] Support range upload (a.k.a 이어올리기)
  - [x] implementation 
  - [x] Update size per object sub classes 
  - [x] exception callback when uploading is broken
  - [x] Add test for classes
  - [x] Adjust class access modifier level
  - [x] Add netty, spring mvc, spring webflux implements
- [storage] Support Copy
  - [x] implementation 
  - [x] hard / soft / replicating hard
- [storage] Support delete operation
  - [x] Support hard-delete, mark-delete

 
### Requirements 2
- [ ] Support content-objectType 
- [ ] image/video thumbnail download
  - when upload (pre-created)
  - on-demand

### Requirements 3
- [ ] support storage tier-ing
- [ ] support object retention policy 
  - [ ] user can change the retention range

### Requirements 4
- [ ] support batch process based on tag
- [ ] support web console

### Undefined 
- [processing] image/video encoding
- [processing] object handling pipeline 
- [delivery] cache layer
- [monitoring] up/download statistics 
- [security] object access objectPolicy  
- [security] content encryption
...


---

jvm options to remove warnings
```
--add-opens
java.base/java.lang=ALL-UNNAMED
--add-opens
java.base/java.util=ALL-UNNAMED
--add-opens
java.base/java.util.function=ALL-UNNAMED
--add-opens
java.base/java.util.stream=ALL-UNNAMED
--add-opens
java.base/java.util.concurrent=ALL-UNNAMED
--add-opens
java.base/java.lang.reflect=ALL-UNNAMED
--add-opens
java.base/java.lang.invoke=ALL-UNNAMED
--add-opens
java.base/java.io=ALL-UNNAMED
```

### links
- image processing server: https://github.com/imgproxy/imgproxy
- image processing server: http://thumbor.org/
- image processing server: https://zauner.nllk.net/post/0032-image-processing-services/

- object storage: https://min.io/ 
- apache benchmarking tool: https://httpd.apache.org/docs/2.4/programs/ab.html (see https://github.com/imgproxy/imgproxy/blob/master/BENCHMARK.md)
- 


### ideas
- java generic은 쉽지 않다...
- interface 계층구조에는 적합하지 않다-> 애초에 이런 의도가 아닌듯. 어떤 클래스가 뭐가 구현될지 모르니까 음...
- sync, async 두 방식을 모두 지원하면서 generic type까지 가져가는 유일한 방법은; sync-object, async-object 각각 나누는 것 밖에 없는 듯...
- 후.. 근데 sync/async 관계 없이 로직 흐름을 그대로 따라가고 싶은데 그러면 코드 중복을 어떻게 제거하지...


- copy, link는 up/download의 usecase 이므로, LinkAwareBinaryRepository 등을 생성하여 구현한다 
- 효율적인 copy는 어떻게 달성하는가? -> 하위 인터페이스 구현을 통해 (FileSystemBinarySupplier -> instance check)

- 일단 ubiquitous language를 먼저 완성해보자.

### notes

mvc
2831989048 bytes

1k
unpooled + direct = 33.9s
unpooled + heap = 25.8s

pooled + direct = 30.0s
pooled + heap = 28.3s


1M
unpooled + direct = 31.01s 
unpooled + heap = 37.7s

pooled + direct = 8.3s
pooled + heap = 7.25s ~ 8.42s


2M
unpooled + direct = 
unpooled + heap = 

pooled + direct = 8.31s 
pooled + heap = 7.98s




10M
unpooled + direct = 
unpooled + heap = 

pooled + direct = over 120s
pooled + heap = over 120s



