-- ============================================
-- 完善的论坛数据插入脚本
-- 包含帖子和评论内容
-- ============================================

USE springtest;

-- 确保有足够的测试用户
INSERT IGNORE INTO user (username, password, email, nickname) VALUES
('traveler1', '123456', 'traveler1@example.com', '提瓦特旅行者'),
('pyro_lover', '123456', 'pyro@example.com', '火系爱好者'),
('explorer_master', '123456', 'explorer@example.com', '探索达人'),
('lore_nerd', '123456', 'lore@example.com', '剧情考据党'),
('gacha_warrior', '123456', 'gacha@example.com', '抽卡玄学大师'),
('kazuha_main', '123456', 'kazuha@example.com', '万叶单推人'),
('nilou_dancer', '123456', 'nilou@example.com', '妮露爱好者'),
('zhongli_sage', '123456', 'zhongli@example.com', '钟离老爷子'),
('furina_star', '123456', 'furina@example.com', '芙宁娜粉丝'),
('raiden_shogun', '123456', 'raiden@example.com', '雷电将军');

-- 确保游戏分区存在，game_id=7 是火影忍者
INSERT IGNORE INTO game_zone (id, name, description, sort_order) VALUES
(1, '原神', '原神游戏专区', 1),
(2, '王者荣耀', '王者荣耀游戏专区', 2),
(3, '英雄联盟', '英雄联盟游戏专区', 3),
(7, '火影忍者', '火影忍者游戏专区', 7);

-- ============================================
-- 插入帖子数据 (gamezone_id=1 是原神)
-- ============================================

-- 纳塔相关帖子
INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '原神新手入门指南', '各位旅行者大家好！作为一个已经玩了两年原神的老玩家，我来给新入坑的小伙伴们分享一些入门心得。

首先，萌新刚进入游戏时，一定要先跟着主线任务走，不要着急去探索大世界。主线任务会引导你熟悉基本操作，还能解锁重要的功能和区域。

资源分配方面，建议前期优先培养一个主C，不要把资源分散给太多角色。推荐新手使用安柏、丽莎、凯亚这些初始角色过渡，等到抽到更好的五星或四星主C后再更换。

冒险等级提升很重要，尽量多做每日委托、刷圣遗物副本、打周本boss。树脂不要浪费，每天都要用完！

最后，祝大家在提瓦特大陆玩得开心！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '纳塔火神瞳收集攻略', '纳塔终于上线了！作为一个收集控，我第一时间就把所有火神瞳找齐了。纳塔的地形真的太复杂了，火山、峡谷、地下洞穴，各种地形都有，没有攻略根本找不全。

我给大家整理了详细的火神瞳收集路线，按区域划分：

1. 沃陆之邦 - 部落联盟区域，主要在地面和悬崖上
2. 火山地带 - 注意躲避熔岩伤害，需要带上保温瓶
3. 夜神之国 - 地下区域，很多神瞳藏在隐蔽的洞穴里
4. 龙之国遗迹 - 需要龙形态才能到达某些高处

每个神瞳我都标注了精确坐标和获取方法，大家跟着走就行！最后祝大家都能顺利收集齐所有火神瞳！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '玛薇卡机制猜想', '看了纳塔的前瞻直播，对新火神玛薇卡的机制非常期待！我来分享一下我的猜想：

首先，从PV中可以看到玛薇卡能够变身为龙形态，这可能是她的核心机制。变身后可能会获得全新的技能组，伤害模式也会改变。

其次，火系反应可能会有大改动。现在的火系反应主要是蒸发、融化、超载，但玛薇卡可能会带来全新的反应机制，比如龙焰之类的。

最后，配队方面，玛薇卡应该会改变现在的配队格局。我预测她可能适合打纯色队，或者与其他龙系角色配合。

以上只是我的个人猜想，具体还要等上线后实测。大家觉得呢？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '纳塔龙形态变身', '纳塔最让我惊喜的就是龙形态变身了！变龙跑图太爽了，纳塔探索体验直接拉满！

每种龙的能力都不一样，设计得真好：
- 火龙：可以飞行，适合快速移动和探索高处
- 岩龙：可以钻地，适合探索地下区域
- 风龙：速度最快，赶路神器

龙形态的技能也好酷炫！特别是火龙的吐息，伤害很高，清怪特别方便。唯一的缺点就是龙形态有时间限制，不能一直保持。

大家最喜欢哪种龙呢？', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '归火圣夜剧情分析', '纳塔的归火圣夜剧情太燃了，我直接看哭了！作为一个剧情党，我来给大家深度分析一下。

首先，剧情中揭露了很多纳塔的历史和龙族的秘密。原来龙族以前这么辉煌啊，壁画的细节解析好强，很多细节我都没注意到。

考据大佬们真是太厉害了，从各种蛛丝马迹中还原出纳塔的历史。龙之国的历史真厚重，感觉可以单独出一个游戏了。

最让我感动的是主角与龙族建立羁绊的过程，那种跨越种族的友谊真的太戳人了。期待后续剧情的发展！', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '沃陆之邦探索指南', '沃陆之邦是纳塔的主要区域之一，这里有很多部落联盟，声望任务好多，这篇指南帮大忙了！

首先，地形探索很容易迷路，有这图就清晰多了。我给大家整理了详细的地图标注，包括：
- 所有传送锚点位置
- 隐藏宝箱位置
- 世界任务触发点
- 部落声望NPC位置

部落联盟的声望任务虽然多，但奖励很丰厚，建议大家优先做。感谢整理，已拿满探索度！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '纳塔新火系角色配队', '纳塔新出的火系角色都太强力了！我来分享几个配队思路：

1. 蒸发队：玛薇卡 + 行秋 + 万叶 + 班尼特 - 伤害爆炸，试了一下非常舒服
2. 超载队：玛薇卡 + 菲谢尔 + 久岐忍 + 万叶 - 打纯水精灵有奇效哈哈
3. 纯色队：玛薇卡 + 香菱 + 班尼特 + 万叶 - 传统火队，稳定输出

终于有火系新配队思路了，感谢！大家还有什么好的配队推荐吗？', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '火山地带生存技巧', '纳塔的火山地带真的太危险了！之前老是被火山灰烫死，学了这招好多了。

生存小技巧分享：
1. 一定要带上保温瓶，可以大幅减少熔岩伤害
2. 尽量走有植被覆盖的区域，那里温度较低
3. 遇到火山喷发时，找掩体躲避
4. 组队探索更安全，可以互相救援

熔岩伤害真高，大家一定要小心！生存小技巧很实用，赞！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '纳塔竞技场挑战', '纳塔的竞技场挑战奖励很丰厚，值得一打！但高难度连战确实有点顶，抄了配队终于过了。

我的配队是：玛薇卡（主C）+ 钟离（护盾）+ 班尼特（治疗/增伤）+ 万叶（聚怪/增伤）

最后那波怪怎么处理？我总是超时。大家有什么好的技巧分享吗？', 'Q&A');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '纳塔特色烧烤食谱', '纳塔的特色烧烤食谱终于拿到了！火辣烤肉的配方加暴击太香了！

食谱分享：
- 火辣烤肉：兽肉*2 + 辣椒*2 + 盐*1 - 暴击率+10%，持续300秒
- 熔岩烤排：兽肉*3 + 辣椒*3 + 奶酪*1 - 暴击伤害+20%，持续300秒
- 龙息烤肉：兽肉*4 + 辣椒*4 + 龙肉*1 - 全属性+10%，持续300秒

纳塔的食物看着都很好吃。已学会，今晚就给角色做一份！', 'SHARE');

-- 继续插入更多帖子...

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '龙之国历史考据', '作为一个考据党，我深入研究了纳塔龙之国的历史，发现了很多有趣的细节！

从壁画上可以看出，龙族以前建立了辉煌的文明，他们掌握了强大的力量。但后来因为某些原因，龙族文明衰落了。

壁画的细节解析好强，龙之国的历史真厚重。考据党狂喜，这篇写得太用心了。大家如果对龙之国历史感兴趣，欢迎一起讨论！', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '纳塔周本Boss打法', '纳塔的周本Boss终于解锁了！Boss机制好烦，但看懂了就好打多了。

逃课技巧分享：
1. 利用地形卡Boss视角
2. 抓住Boss的硬直时间输出
3. 钟离的护盾可以无视很多机制

逃课技巧绝了，不用再坐牢了。感谢分享，一次就过。大家还有什么好的逃课技巧吗？', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '夜神之国解密', '夜神之国的地下世界太绕了，这篇跑图攻略是救星！

解密卡了好久，看了攻略才明白。夜神之国的氛围感绝了，阴森恐怖但又很吸引人。

主要解密类型：
1. 符文谜题：按顺序激活符文
2. 光影谜题：利用光源照亮特定区域
3. 机关谜题：找到正确的开关顺序

大家在夜神之国卡关了吗？', 'Q&A');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '纳塔锻造武器测评', '纳塔新出了几把锻造武器，我来给大家测评一下！

测试结果：
1. 龙息大剑：适合物理主C，精炼5阶性价比很高
2. 火山弓：适合蓄力弓箭角色
3. 熔岩法器：适合法师站场输出

哪把锻造武器最值得做？看了测评心里有数了。感觉这把大剑给老角色也不错。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '新四星火系大剑', '纳塔新出的四星大剑角色太强力了！这四星强度可以啊，平民战神。

技能倍率很高，特别是元素爆发，伤害真的爆炸。培养材料已经刷好了，就等上线。

满命的话能碰瓷五星吗？大家觉得这个角色值得抽吗？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '纳塔地灵龛位置', '纳塔的地灵龛我已经全部找到了！10个地灵龛全开，拿到了不少原石！

有几个位置太阴间了，感谢指路。特别是悬崖上的那几个，没有攻略根本找不到。

刚好缺钥匙，顺便把任务做了。地灵龛给的原石奖励还是很丰厚的，大家记得找全！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '派蒙在纳塔的剧情', '派蒙这次在纳塔的剧情里居然有了新能力？太可爱了吧！

派蒙不只是一只应急食品了哈哈。剧情里派蒙的高光时刻真的很多，她和龙族的互动特别有趣。

派蒙的新能力好像和龙族有关，具体是什么我就不剧透了，大家自己去体验吧！', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '纳塔跑酷路线分享', '纳塔的地形太适合跑酷了！钩锁加龙形态跑图太丝滑了。

我给大家设计了一条最优跑酷路线：
起点：沃陆之邦传送锚点
路线：利用钩锁爬上悬崖 → 变龙飞行 → 穿越火山地带 → 到达夜神之国入口

这条路线设计得好，节省一半时间。极限跑图玩家来报道！', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '纳塔每日委托成就', '纳塔的每日委托有很多隐藏成就！隐藏成就居然这么多，之前都没触发。

我给大家汇总了一下：
1. 烧烤大师：完成10次烧烤委托
2. 龙族之友：完成所有龙族相关委托
3. 火山探险家：完成所有火山地带委托

每天白嫖原石的机会不能错过。感谢汇总，已全部解锁！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '火系反应大改？', '最近有传闻说火系反应要大改？如果改了那火系又要起飞了。

我来分析一下：
- 可能会加强超载反应的伤害
- 可能会新增火系专属反应
- 可能会调整蒸发和融化的倍率

分析得很中肯，感觉会有微调。希望不要削弱胡桃啊！大家觉得呢？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '纳塔悬崖宝箱', '纳塔的悬崖宝箱太难拿了，手残党狂哭！

我给大家分享几个拿宝箱的技巧：
1. 利用四叶印快速移动
2. 变龙飞行直接拿
3. 利用角色的位移技能

拿到宝箱的一刻太有成就感了。感谢坐标分享，少走很多弯路。大家还有什么好的技巧吗？', 'Q&A');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '基尼奇速切玩法', '基尼奇这个角色的机制很有意思！我来分享一下速切流玩法。

配装思路：
- 武器：祭礼大剑
- 圣遗物：宗室4件套
- 天赋：优先点元素战技和元素爆发

大招伤害真高，速切流很顺畅。配装思路学到了，马上回去试。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '纳塔龙族图鉴', '纳塔的龙族图鉴我已经全部收集完成了！收集控狂喜，每种龙都好帅！

图鉴整理得好全，辛苦了。包含：
- 火龙：攻击力最高
- 岩龙：防御力最高
- 风龙：速度最快
- 水龙：可以潜水

最喜欢那条能飞的龙。大家收集齐了吗？', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '纳塔隐藏世界任务', '纳塔有很多没有提示的隐藏世界任务！没有提示的世界任务是怎么发现的？太强了。

我给大家分享几个隐藏任务的触发方法：
1. 特定时间在特定地点等待
2. 完成前置任务后解锁
3. 与特定NPC对话多次

超长任务做下来剧情很感人。感谢攻略，不然绝对错过。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '火之国结局预测', '我来大胆预测一下纳塔的结局！预测很大胆，希望别刀我。

根据目前的剧情线索，我觉得：
- 主角会帮助龙族复兴
- 玛薇卡会成为新的火神
- 纳塔会与其他国家建立友好关系

旅行者面临的挑战肯定不简单。期待纳塔结局，千万别发刀！', 'DISCUSSION');

-- ============================================
-- 枫丹相关帖子
-- ============================================

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '枫丹水神瞳全收集', '枫丹的水神瞳我终于全收集了！水下神瞳太折磨人了，这篇指南太及时。

我给大家整理了详细的收集路线，按区域划分：
1. 枫丹廷区域 - 主要在建筑上
2. 水下区域 - 需要潜水才能到达
3. 海底遗迹 - 很多神瞳藏在遗迹里

终于全收集了，强迫症舒服了。水下路线画得很清晰，赞！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '芙宁娜增伤机制分析', '芙宁娜的增伤机制终于搞懂了！气氛值怎么叠，写得真好。

详细分析：
- 气氛值通过技能命中敌人积累
- 气氛值越高，增伤效果越强
- 需要控制血量来维持气氛值

烧血增伤机制好独特，芙宁娜太强了。看完马上调整了配队思路。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '枫丹潜水玩法详解', '枫丹的水下探索太好玩了，就是体力有点不够。

新手必看，水下机制讲解得很清楚：
1. 水下技能组合很丰富
2. 宝箱也很多
3. 可以与原海异种战斗

水下技能组合很丰富，宝箱也多。大家喜欢水下探索吗？', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '林尼纯火队配装', '林尼的纯火队太强了！重击融化伤害看不懂，太爆炸了。

配装分享：
- 林尼：暴击头 + 火伤杯 + 攻击沙
- 班尼特：宗室4件套
- 万叶：翠绿4件套
- 钟离：千岩4件套

纯火队配装学到了，林尼玩家必看。一风带三火的思路很赞。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '枫丹科学院解谜', '枫丹科学院的荒芒能量解谜有点绕，看了攻略才明白。

解谜思路：
1. 理解荒芒能量的属性
2. 找到正确的能量源
3. 按顺序激活机关

发条机关的原理原来是这样。科学院的谜题设计得很有趣。', 'Q&A');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '那维莱特重击技巧', '那维莱特的重击技巧分享！源水之滴的吸收范围原来这么大！

技巧详解：
1. 尽量在有多个源水之滴的地方使用重击
2. 可以通过移动来调整重击范围
3. 配合芙宁娜效果更佳

转圈圈重击太爽了，清怪神技。感谢技巧分享，输出提升明显。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '枫丹大世界特殊宝箱', '枫丹有很多需要荒芒属性解锁的特殊宝箱！荒芒属性解锁的宝箱差点错过了。

宝箱位置很全，已拿满！特殊宝箱给的奖励还不错，大家记得找全。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '琳妮特跑图流', '琳妮特的E技能跑图真的快，大世界好帮手。

跑图技巧：
1. 利用E技能的长距离位移
2. 配合四叶印快速移动
3. 可以在空中再次释放E技能

终于有个好用的跑图角色了。长距离位移太舒服了。', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '枫丹锻造武器推荐', '枫丹的锻造武器我来给大家推荐一下！哪把最值得做已经很明显了。

推荐列表：
1. 灰河渡手 - 适合那维莱特
2. 静谧之曲 - 适合琳妮特
3. 浪影阔剑 - 适合物理主C

锻造武器里这把性价比最高。感谢推荐，材料已经备齐了。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '菲米努冰系物理流', '菲米努的冰系物理流居然能玩？太有创意了。

配队思路：
- 菲米努：物理冰伤
- 罗莎莉亚：减抗+暴击
- 班尼特：增伤+治疗
- 钟离：护盾

超导减防后伤害确实可以。整活配队，但是意外的好用。', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '枫丹律法与剧情梳理', '枫丹的剧情深度真的高，梳理得很清晰。

剧情亮点：
1. 审判和谕示机的背景考据太强了
2. 芙宁娜的角色弧光很完整
3. 那维莱特的身世揭秘

剧情党狂喜，这篇必看！大家觉得枫丹剧情怎么样？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '水下Boss战技巧', '枫丹的水下Boss战技巧分享！水下打巨型怪真不好操作，技巧很有用。

战斗技巧：
1. 利用水下技能躲避攻击
2. 抓住Boss的弱点输出
3. 注意氧气消耗

掌握技巧后简单多了。原海异种打起来真费劲。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '夏洛蒂挂冰辅助', '夏洛蒂作为四星挂冰辅助，性价比很高。

角色分析：
- 脱手挂冰频率不错
- 可以给队伍增伤
- 值得培养

终于有个好用的冰系法器辅助了。大家在用夏洛蒂吗？', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '枫丹城屋顶神瞳', '枫丹城屋顶的神瞳上去的路线好难找，感谢分享。

最快路线确实快，少走弯路。枫丹廷上面风景不错！大家上去过吗？', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '枫丹特色料理食谱', '枫丹的特色料理食谱分享！bulle汤的配方终于拿到了，加暴击好用。

食谱列表：
- bulle汤：暴击率+10%
- 炸鱼排：攻击力+15%
- 千层酥：防御力+20%

炸鱼排制作简单，性价比高。料理加成对深渊有帮助。', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '白术在枫丹体系的作用', '白术在枫丹体系的作用太大了！白术和芙宁娜配合太好了，解决烧血问题。

配队分析：
- 白术提供持续治疗
- 芙宁娜提供增伤
- 两者配合完美

终于知道白术的就业岗位了。回复配合增伤，思路很赞。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '枫丹地灵龛位置', '枫丹的地灵龛位置分享！10个地灵龛全开，原石到手！

位置很准确，没这攻略找不全。感谢分享，省了不少时间。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '港口锚点解锁路线', '枫丹港口锚点的解锁路线分享！锚点全开跑图才方便，路线很合理。

水下锚点很容易漏，感谢指路。开图必看，很详细。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '原海异种掉落物', '原海异种掉落物速刷路线分享！异海凝珠速刷路线太需要了，感谢！

速刷路线：
1. 枫丹港口附近
2. 水下遗迹区域
3. 特定时间刷新点

刷材料的痛苦谁懂，这篇救了大命。效率很高，已刷满。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '枫丹日报成就', '枫丹日报成就汇总！报纸位置太难找了，这篇汇总很全。

成就列表：
1. 报刊收藏家：收集所有报纸
2. 每日阅读：连续7天阅读报纸
3. 新闻爱好者：阅读所有特殊新闻

隐藏成就终于拿到了。感谢整理，阅读报纸也有成就。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 1, '那维莱特单通深渊', '那维莱特单通深渊12层！满命水龙太强了，转圈圈就完事了。

单通12层，这角色真离谱。看着解压，但我知道我抽不到满命。大家觉得那维莱特强度怎么样？', 'SHARE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 2, '水仙十字结社任务', '水仙十字结社系列任务攻略！系列任务好长，但这篇流程很清晰。

剧情信息量好大，看攻略才理顺。全流程跟下来很有成就感。', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 3, '枫丹海底遗迹解密', '枫丹海底遗迹解密攻略！符文解谜思路很清晰，一次过。

古老祭坛的设计很有意思。没这解谜思路我肯定卡关了。', 'Q&A');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 4, '旅行剑在水下', '旅行剑在水下居然有特殊效果？测试得很详细，涨知识了。

测试结果：
1. 水下攻击范围变大
2. 技能形态改变
3. 可以触发特殊反应

旅行剑在水下居然有特殊效果？大家发现了吗？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(1, 5, '梅洛彼得堡剧情', '梅洛彼得堡的剧情太刺激了！地下监狱的逃生剧情太刺激了。

剧情深度不错，很感人。跟着攻略走，没漏掉任何细节。', 'SHARE');

-- ============================================
-- 火影忍者相关帖子 (gamezone_id=7)
-- ============================================

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 1, '火影忍者新手入门指南', '作为一个火影忍者老粉丝，我来给新入坑的小伙伴们分享一些入门心得！

火影忍者的世界真的太精彩了，从木叶村开始，我们将跟随鸣人一起成长。新手入门需要注意：

1. 先看主线剧情，了解世界观
2. 记住主要角色和他们的能力
3. 理解查克拉和忍术的基本概念

祝大家在火影的世界里玩得开心！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 2, '宇智波一族的秘密', '宇智波一族是火影里最神秘的家族之一！写轮眼、须佐能乎、伊邪那岐，各种强大的能力。

宇智波一族的历史充满了悲剧，但他们的力量确实让人敬畏。从斑到佐助，宇智波一族的故事贯穿了整部火影。

大家觉得宇智波一族最强的是谁？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 3, '鸣人的成长历程', '鸣人的成长历程真的太励志了！从一个被全村人讨厌的孤儿，到成为拯救世界的英雄。

鸣人的成长告诉我们，只要坚持自己的忍道，就一定能实现目标。从九尾人柱力到七代目火影，鸣人的每一步都不容易。

大家最喜欢鸣人的哪个时期？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 4, '晓组织成员实力排名', '晓组织作为火影里的反派组织，每个成员都有独特的能力！我来给大家排个名：

1. 佩恩：神罗天征太强了
2. 宇智波鼬：须佐能乎+天照
3. 大蛇丸：不死之身
4. 角都：五颗心脏
5. 迪达拉：艺术就是爆炸

大家觉得这个排名合理吗？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 5, '忍术结印大全', '火影里的忍术结印真的太帅了！我来给大家整理一下常见忍术的结印方法：

1. 火遁·豪火球之术：巳-未-申-亥-午-寅
2. 水遁·水龙弹之术：丑-辰-巳-午-未-申-亥-子-丑-寅-卯-辰-巳
3. 雷遁·雷切：丑-卯-申

结印虽然复杂，但真的很有仪式感！', 'GUIDE');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 1, '五影会谈背后的阴谋', '五影会谈这段剧情真的太精彩了！表面上是五大国的和平会谈，实际上暗流涌动。

佐助的突袭、带土的宣战、团藏的阴谋，各种事件交织在一起。这段剧情把火影的政治斗争推向了高潮。

大家觉得五影会谈最精彩的部分是什么？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 2, '我爱罗的砂之守护', '我爱罗的转变真的太感人了！从一个杀人不眨眼的怪物，到成为守护村子的风影。

鸣人的影响改变了我爱罗，让他明白了守护的意义。砂之守护不仅是一种能力，更是一种信念。

大家喜欢我爱罗吗？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 3, '卡卡西的写轮眼', '卡卡西作为拷贝忍者，他的写轮眼真的太强了！虽然不是宇智波一族，但他把写轮眼用得淋漓尽致。

神威、雷切、拷贝上千种忍术，卡卡西的实力绝对不容小觑。虽然经常缺蓝，但关键时候从来不掉链子。

大家觉得卡卡西的写轮眼用得怎么样？', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 4, '纲手的医疗忍术', '纲手作为木叶三忍之一，她的医疗忍术是火影里最强的！创造再生、百豪之术，各种逆天的医疗忍术。

纲手不仅医术高明，战斗力也很强。怪力拳一拳下去，连墙壁都能打穿。作为五代目火影，她确实很称职。', 'DISCUSSION');

INSERT INTO post (gamezone_id, user_id, title, content, post_type) VALUES
(7, 5, '自来也豪杰物语', '自来也的豪杰物语真的太好哭了！作为鸣人的师父，他教会了鸣人很多东西。

从妙木山的修行，到佩恩之战的牺牲，自来也的一生都在为和平而奋斗。他的豪杰物语虽然结束了，但他的精神永远传承下去。

大家觉得自来也是火影里最伟大的角色吗？', 'DISCUSSION');

-- ============================================
-- 插入评论数据
-- ============================================

-- 为帖子1添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(1, 2, '萌新刚入坑，感谢大佬指路！'),
(1, 3, '写得真好，已经收藏了慢慢看。'),
(1, 4, '这指南太全了，刚下载游戏正需要这个。');

-- 为帖子2添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(2, 1, '纳塔地形太复杂了，没这攻略根本找不全。'),
(2, 2, '感谢分享，终于把火神瞳凑齐了！'),
(2, 3, '之前漏了两个，看了攻略才找到。');

-- 为帖子3添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(3, 1, '如果真是这样那火神必抽啊！'),
(3, 2, '分析得很有道理，期待上线实装。'),
(3, 3, '感觉火神会改变现在的配队格局。');

-- 为帖子4添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(4, 1, '变龙跑图太爽了，纳塔探索体验拉满。'),
(4, 2, '龙形态的技能好酷炫！'),
(4, 3, '每种龙的能力都不一样，设计得真好。');

-- 为帖子5添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(5, 1, '纳塔的剧情太燃了，看哭了。'),
(5, 2, '考据大佬牛逼，很多细节我都没注意到。'),
(5, 3, '期待后续剧情的发展！');

-- 为帖子6添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(6, 1, '部落联盟的声望任务好多，这篇指南帮大忙了。'),
(6, 2, '地形探索很容易迷路，有这图就清晰多了。'),
(6, 3, '感谢整理，已拿满探索度！');

-- 为帖子7添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(7, 1, '蒸发队伤害爆炸，试了一下非常舒服。'),
(7, 2, '超载队打纯水精灵有奇效哈哈。'),
(7, 3, '终于有火系新配队思路了，感谢！');

-- 为帖子8添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(8, 1, '之前老是被火山灰烫死，学了这招好多了。'),
(8, 2, '熔岩伤害真高，保温瓶必备。'),
(8, 3, '生存小技巧很实用，赞！');

-- 为帖子9添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(9, 1, '高难度连战有点顶，抄了配队终于过了。'),
(9, 2, '奖励很丰厚，值得一打。'),
(9, 3, '最后那波怪怎么处理？我总是超时。');

-- 为帖子10添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(10, 1, '火辣烤肉的配方终于拿到了，加暴击太香了。'),
(10, 2, '纳塔的食物看着都很好吃。'),
(10, 3, '已学会，今晚就给角色做一份！');

-- 为帖子11添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(11, 1, '壁画的细节解析好强，纳塔的历史真厚重。'),
(11, 2, '原来龙族以前这么辉煌啊。'),
(11, 3, '考据党狂喜，这篇写得太用心了。');

-- 为帖子12添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(12, 1, '逃课技巧绝了，不用再坐牢了。'),
(12, 2, 'Boss机制好烦，看懂了就好打多了。'),
(12, 3, '感谢分享，一次就过了。');

-- 为帖子13添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(13, 1, '地下世界太绕了，这篇跑图攻略是救星。'),
(13, 2, '解密卡了好久，看了攻略才明白。'),
(13, 3, '夜神之国的氛围感绝了。');

-- 为帖子14添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(14, 1, '哪把锻造武器最值得做？看了测评心里有数了。'),
(14, 2, '感觉这把大剑给老角色也不错。'),
(14, 3, '精炼5阶性价比很高。');

-- 为帖子15添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(15, 1, '这四星强度可以啊，平民战神。'),
(15, 2, '培养材料已经刷好了，就等上线。'),
(15, 3, '满命的话能碰瓷五星吗？');

-- 为帖子16添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(16, 1, '10个地灵龛全开，拿到了不少原石！'),
(16, 2, '有几个位置太阴间了，感谢指路。'),
(16, 3, '刚好缺钥匙，顺便把任务做了。');

-- 为帖子17添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(17, 1, '派蒙这次居然有了新能力？太可爱了吧！'),
(17, 2, '派蒙不只是一只应急食品了哈哈。'),
(17, 3, '剧情里派蒙的高光时刻！');

-- 为帖子18添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(18, 1, '钩锁加龙形态跑图太丝滑了。'),
(18, 2, '这条路线设计得好，节省一半时间。'),
(18, 3, '极限跑图玩家来报道！');

-- 为帖子19添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(19, 1, '隐藏成就居然这么多，之前都没触发。'),
(19, 2, '每天白嫖原石的机会不能错过。'),
(19, 3, '感谢汇总，已全部解锁！');

-- 为帖子20添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(20, 1, '如果改了那火系又要起飞了。'),
(20, 2, '分析得很中肯，感觉会有微调。'),
(20, 3, '希望不要削弱胡桃啊！');

-- 为帖子21添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(21, 1, '攀岩宝箱太难拿了，手残党狂哭。'),
(21, 2, '拿到宝箱的一刻太有成就感了。'),
(21, 3, '感谢坐标分享，少走很多弯路。');

-- 为帖子22添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(22, 1, '大招伤害真高，速切流很顺畅。'),
(22, 2, '配装思路学到了，马上回去试。'),
(22, 3, '基尼奇这机制很有意思。');

-- 为帖子23添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(23, 1, '收集控狂喜，每种龙都好帅！'),
(23, 2, '图鉴整理得好全，辛苦了。'),
(23, 3, '最喜欢那条能飞的龙。');

-- 为帖子24添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(24, 1, '没有提示的世界任务是怎么发现的？太强了。'),
(24, 2, '超长任务做下来剧情很感人。'),
(24, 3, '感谢攻略，不然绝对错过。');

-- 为帖子25添加评论
INSERT INTO comment (post_id, user_id, content) VALUES
(25, 1, '预测很大胆，希望别刀我。'),
(25, 2, '旅行者面临的挑战肯定不简单。'),
(25, 3, '期待纳塔结局，千万别发刀！');

-- ============================================
-- 完成！
-- ============================================
