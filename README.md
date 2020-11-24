# Table of Contents


## About
객체지향과 클린 아키텍처에서 강조하는 원칙들과 DDD 패턴을 가지고 실제 비즈니스 요구사항들을 구현한다.
이 프로젝트는 단순히 end-user 서비스 요구사항이 아닌, 보다 플랫폼의 측면에서 요구사항을 정리하고 구현한다


### Principles 원칙



## Domain: 이 소프트웨어는 무엇을 해결하려 하는가?
짧은 답변: 서비스 개발에는 "다양한" 기능이 있는 네트워크 파일 스토리지가 필요하다

긴 답변: |
서비스 개발을 하다보면 이미지를 사용할 일은 많다. 동영상은 이미지보다는 아니지만 요즘에는 꽤 고해상도의 영상까지 자주 사용되고 있다. 뿐만 아니라 어떤 사용자는 ms excel 파일이나 log 같은 텍스트만을, 어떤 팀은 zip와 같은 압축파일 바이너리를 어딘가에 두고 언제든지 사용하고 싶어한다. 그러나 이러한 파일들을 어디에 저장하고 또 어떻게 서비스에 사용한단 말인가? 서비스에서는 비즈니스 로직이 중요하므로, 필요한 파일을 읽고 쓰는데는 최소한의 오버헤드만 발생해야 하고, 또 이를 사용하는 방법도 매우 단순하길 원할 것이다. 

이 소프트웨어는 이러한 요구사항에 대한 궁극적인 답변이자 서비스에서 원하는 형태의 소프트웨어이다. 빠르며, 이해하기 쉽고, 미디어 파일에 대해 다양한 기능을 지원하며, 새로운 기능을 추가하고 확장하기 용이한 형태의 구조를 가지고 있다. 단순히 이진 바이너리를 저장하는 네트워크 스토리지 시스템에서부터, 이미지를 자르고 비디오를 인코딩하고 파일 포맷을 변경하고 접근 빈도에 가장 적합한 네트워크 스토리지를 자동으로 적용하고 미디어를 분석하여 서비스에서 손쉽게 결과를 가져다 사용할 수 있는 올인원 미디어 프로세싱 스토리지까지 여러 모습을 두루 갖추고 있는 완성형 소프트웨어가 서비스 팀에게 필요하다. 


## Domain description (TODO: in english)
유저는 object storage에 파일을 업로드하거나 다운로드 할 수 있다. 언제나 업로드 할 수 있는 것은 
업로드 된 파일은 



미디어 파일은 




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



