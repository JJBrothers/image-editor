이미지에디터를 모든 프로젝트에서 component로 호출할수있도록
module project로 개발하였다.

통신모듈은 retrofit을,
권한 체킹은 ted를,
이미지 자르기 cropping은 github open source를
이미지 회전효과는 직접 구현하였다.

glide를 너무무분별하게 사용한나머지 ..
이미지에대한 커스터마이징에 한계가있다 ..
glide를 base로한 필터라이브러리를 적용하였으며
out of memory(OOM) 을 막느라 얼마나 노력을했는지모른다 .
어느정도 안정화가 되었으나, 돌이킬수없는 실수를 저질렀으니.....
시간나면 처음부터 다시만들도록한다 ...
gilde가 분명 좋은 라이브러리임에는 틀림이없지만
수동으로 image를 sampling 하여 최대한 메모리를 줄일수있도록 dynamic하게 sampling 하는것이
내가내린 최고의 판단이다 .
glide는 캐싱용도의 라이브러리로 사용하는것이 제일 best 인듯하고
현재 이프로젝트에서는 이미지 선택grid에서만 사용되는것이 가장 좋은 방법일듯하다
이미지 효과를 주는 부분은... 앞서언급한것처럼 수동으로 dynamic하게 sampling을하도록하고
이미지 효과를 적용하는 부분은 openGL을 직접 공부하여 개발하거나, renderscript를 공부해야한다 ..
참고로 renderscript는 c++ 코드이니 ...앞길이 막막하다
카카오톡과 똑같이 만들고싶었으나 이대로 접는다 더이상 힘이나지않는다
언젠가는 다시도전하리
