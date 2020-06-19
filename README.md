# gmall2020   本地修改版本提交github

# gmall-user用户服务8080
dubbo-admin(用户名密码都是root)            dubbo服务检测中心    :8080
gmall-user-service                       用户服务的service层  :8070
gmall-user-web                           用户服务的web层      :8081

gmall-admin(前端项目,用户名密码都是admin)   谷粒商城后台管理系统  :8888

gmall-manage-service                     用户服务的service层  :8071
gmall-manage-web                           用户服务的web层    :8082

fastdfs中tracker-server                  fastdfs服务跟踪端口  :22122
fastdfs中storage-server                  fastdfs服务存储端口  :23000

nginx                                    nginx服务期端口      :8099
##http://192.168.2.170:8099/group1/M00/00/00/wKgCql6QT2mAPOCfAAXraBufj30973_big.jpg

#gmall-item-servier                       前台系统的商品详情服务（实际可以调用gmall-manage-service实现）
gmall-item-web                           前台系统的商品详情展示 :8083

gmall-redisson-test                       redisson测试       :8180
elasticSearch                             es非关系型数据库     :9200
kibana       用来搜索、查看交互存储在Elasticsearch索引中的数据    :5601

gmall-search-service                     搜素服务的service层   :8072
gmall-search-web                         搜索服务的web层       :8084

gmall-cart-service                       购物车服务的service层 :8073
gmall-cart-web                           购物车服务的web层     :8085

gmall-passport-web                       用户认证中心web层     :8086
#gmall-passport-service(gmall-user-service)

gmall-order-web                          订单服务的web层       :8087
gmall-order-service                      订单服务的service层   :8074

gmall-payment                            订单支付业务          :8075

ActiveMQ                                 消息队列中间件        :8161

gware-manage                             库存管理服务          :9001

gmall-seckill                            秒杀服务             :8076

项目未完成的功能点，仍需后期开发
1.后台管理保存商品信息后需要发送队列消息，同步到缓存和elasticsearch中
2.搜索商品时，热度值字段--方案：可以将热度值字段，单独存储在redis，在redis中放置一个热度值字段，对应es中的热度值；在es检索结果之后，根据商品id，
取出热度值，根据它进行排序
3.购物车模块，如果用户登陆，合并cookie中和db中的购物车数据，然后同步redis
在用户登陆时，发出一个用户登陆的消息（话题消息topic），让cartService消费，做购物车的合并和同步缓存
在访问购物车列表时，如果当前用户已经登陆，则删除cookie中多余的购物车数据
4.提交订单
调用库存服务的库存查询接口，做库存的校验
5.库存削减的队列
由订单服务消费，订单服务修改订单状态为准备出库