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



