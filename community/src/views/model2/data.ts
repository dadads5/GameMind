export const GAME_DATA = {
  characters: [
    {
      id: 'ironclad',
      name: '铁甲战士',
      title: 'Ironclad',
      color: 'text-red-500',
      borderColor: 'border-red-500',
      bgLight: 'bg-red-500/10',
      description: '由于出卖灵魂而获得了恶魔的力量。他擅长使用强大的攻击和坚实的防御，能够通过战斗中产生的愤怒（力量）来摧毁敌人。',
      mechanics: ['力量 (Strength)', '易伤 (Vulnerable)', '消耗 (Exhaust)', '自我治愈'],
      difficulty: '简单',
      recommendedDecks: ['力量流', '防战流', '消耗流'],
      strategy: '核心思路通常围绕积累力量或利用消耗机制。在《杀戮尖塔2》中，铁甲战士的打击感更加厚重，新增了一些与“伤口”互动的新卡牌。',
      icon: 'fa-gavel',
    },
    {
      id: 'silent',
      name: '静默猎手',
      title: 'The Silent',
      color: 'text-green-500',
      borderColor: 'border-green-500',
      bgLight: 'bg-green-500/10',
      description: '来自雾地的致命猎人。擅长使用毒素、飞刀（Shivs）和敏捷的闪避。她能在单回合内打出大量卡牌，让敌人在无声中消亡。',
      mechanics: ['中毒 (Poison)', '小刀 (Shivs)', '弃牌 (Discard)', '虚弱'],
      difficulty: '中等',
      recommendedDecks: ['毒素流', '无限飞刀流', '弃牌爆发流'],
      strategy: '利用大量的抽牌和弃牌逻辑来循环卡组。在续作中，静默猎手的飞刀体系得到了强化，新增了更多与“连击”相关的奖励。',
      icon: 'fa-skull-crossbones',
    },
    {
      id: 'necrobinder',
      name: '死灵术师',
      title: 'The Necrobinder',
      color: 'text-purple-500',
      borderColor: 'border-purple-500',
      bgLight: 'bg-purple-500/10',
      description: '《杀戮尖塔2》全新角色。她是一位被诅咒的法师，能操纵死者的遗骸。她不直接战斗，而是通过召唤骷髅和消耗“灵魂能量”来主宰战场。',
      mechanics: ['召唤骷髅', '灵魂能量 (Soul)', '牺牲 (Sacrifice)', '骨甲'],
      difficulty: '困难',
      recommendedDecks: ['亡灵大军流', '牺牲爆破流', '灵魂掌控流'],
      strategy: '管理你的召唤物队列是关键。骷髅可以吸收伤害并进行反击。通过牺牲低级骨架来释放威力惊人的亡灵法术。',
      icon: 'fa-wand-sparkles',
    },
    {
      id: 'defect',
      name: '机器人',
      title: 'The Defect',
      color: 'text-blue-500',
      borderColor: 'border-blue-500',
      bgLight: 'bg-blue-500/10',
      description: '觉醒了自我意识的古老自动机。通过生成并激发各种属性的充能球（Orbs）来施展魔法。它是科技与魔法的结合体。',
      mechanics: ['充能球 (Orbs)', '集中 (Focus)', '能力牌 (Powers)', '闪电/冰霜/等'],
      difficulty: '中等',
      recommendedDecks: ['万物一心流', '极简电球流', '无限冰球防御流'],
      strategy: '建立球位并提高集中力。在2代中，机器人的物理攻击手段有所增加，使其在没有球位时也能具备一定的战斗力。',
      icon: 'fa-microchip',
    },
    {
      id: 'primarch',
      name: '储君',
      title: 'The Primarch',
      color: 'text-amber-500',
      borderColor: 'border-amber-500',
      bgLight: 'bg-amber-500/10',
      description: '拥有皇室血统的剑士，擅长通过精准的姿态切换来应对战局。他能通过积累“威严”来强化自己的招式，展现出无可匹敌的统率力。',
      mechanics: ['姿态切换 (Stance)', '威严 (Majesty)', '御剑术', '王之宝库'],
      difficulty: '中等',
      recommendedDecks: ['姿态舞动流', '威严爆发流', '无限御剑流'],
      strategy: '在不同的姿态间灵活切换是储君的核心玩法。进攻姿态提供额外伤害，防御姿态提供高额减伤。通过威严点数可以瞬间释放大招。',
      icon: 'fa-crown',
    },
  ],
  cards: [
    { id: 1, charId: 'ironclad', name: '重刃', cost: 2, rarity: 'common', type: '攻击', description: '造成 14 点伤害。力量对该牌的效果提高 3(5) 倍。', combo: '与【观察弱点】或【充能极限】配合效果极佳。' },
    { id: 2, charId: 'ironclad', name: '恶魔形态', cost: 3, rarity: 'rare', type: '能力', description: '在你的每回合开始时，获得 2(3) 点力量。', combo: '铁甲战士的核心成长牌，配合高防体系拖入后期。' },
    { id: 9, charId: 'ironclad', name: '旋风斩', cost: -1, rarity: 'uncommon', type: '攻击', description: '消耗所有能量。对所有敌人造成 8(11) 点伤害 X 次。', combo: '清理杂兵的神技，配合【双发】可以造成双倍打击。' },
    { id: 10, charId: 'ironclad', name: '全身撞击', cost: 1, rarity: 'common', type: '攻击', description: '造成等同于你当前格挡值的伤害。', combo: '防战流的核心输出手段，配合【壁垒】效果恐怖。' },
    { id: 21, charId: 'ironclad', name: '双发', cost: 1, rarity: 'rare', type: '技能', description: '你本回合打出的下一张(两张)攻击牌将打出两次。', combo: '配合【重刃】或【旋风斩】爆发极高。' },
    { id: 22, charId: 'ironclad', name: '燃烧', cost: 1, rarity: 'uncommon', type: '能力', description: '获得 2(3) 点力量。', combo: '简单粗暴的力量来源。' },
    { id: 3, charId: 'silent', name: '爆发', cost: 1, rarity: 'rare', type: '技能', description: '你本回合打出的下一张(两张)技能牌将打出两次。', combo: '配合【催化剂】可以瞬间让敌人的中毒层数翻倍。' },
    { id: 4, charId: 'silent', name: '精准', cost: 1, rarity: 'uncommon', type: '能力', description: '小刀造成的伤害增加 4(6) 点。', combo: '飞刀流的核心，让低费的小刀具备恐怖的输出。' },
    { id: 11, charId: 'silent', name: '催化剂', cost: 1, rarity: 'uncommon', type: '技能', description: '将一名敌人的中毒层数翻 2(3) 倍。消耗。', combo: '毒素流的终极杀招，配合【爆发】能产生天文数字般的伤害。' },
    { id: 12, charId: 'silent', name: '余像', cost: 1, rarity: 'rare', type: '能力', description: '每当你打出一张牌，获得 1 点格挡。', combo: '无限流和飞刀流的防御神卡，在海量出牌的同时保证生存。' },
    { id: 23, charId: 'silent', name: '斗篷与短剑', cost: 1, rarity: 'common', type: '技能', description: '获得 6 点格挡。将 1(2) 张小刀放入你的手牌。', combo: '攻守兼备，飞刀流的优质启动牌。' },
    { id: 24, charId: 'silent', name: '毒雾', cost: 1, rarity: 'uncommon', type: '能力', description: '在你的回合开始时，给所有敌人层 2(3) 层中毒。', combo: '毒素流的稳定消耗手段。' },
    { id: 5, charId: 'necrobinder', name: '白骨屏障', cost: 1, rarity: 'common', type: '技能', description: '获得 8 点格挡。如果本回合有召唤物死亡，格挡翻倍。', combo: '防守核心，鼓励玩家主动牺牲或消耗骷髅。' },
    { id: 6, charId: 'necrobinder', name: '亡者大军', cost: 3, rarity: 'rare', type: '攻击', description: '对所有敌人造成 5 点伤害 X 次，X 为你当前召唤物的数量。', combo: '爆发手段，在铺满全场召唤物后使用。' },
    { id: 13, charId: 'necrobinder', name: '灵魂收割', cost: 1, rarity: 'uncommon', type: '攻击', description: '造成 9 点伤害。如果敌人死亡，获得 3 点灵魂能量。', combo: '获取资源的主要手段，用于支持高费的亡灵法术。' },
    { id: 14, charId: 'necrobinder', name: '骨骸重生', cost: 2, rarity: 'rare', type: '技能', description: '复活本局战斗中死亡的所有召唤物。', combo: '后期神卡，瞬间铺满全场，配合【亡者大军】终结战斗。' },
    { id: 25, charId: 'necrobinder', name: '灵魂冲击', cost: 1, rarity: 'common', type: '攻击', description: '造成 7 点伤害。消耗 1 点灵魂能量：伤害翻倍。', combo: '灵活的输出手段。' },
    { id: 26, charId: 'necrobinder', name: '禁忌契约', cost: 0, rarity: 'uncommon', type: '技能', description: '牺牲一个召唤物，抽 2(3) 张牌。', combo: '强大的过牌能力，同时触发死亡相关的效果。' },
    { id: 7, charId: 'defect', name: '冰川', cost: 2, rarity: 'uncommon', type: '技能', description: '获得 7 点格挡。生成 2 个冰霜充能球。', combo: '防御神牌，瞬间提供大量格挡并建立防线。' },
    { id: 8, charId: 'defect', name: '回响形态', cost: 3, rarity: 'rare', type: '能力', description: '每回合你打出的第一张牌将打出两次。', combo: '全游戏最强成长牌之一，让任何强力单卡效果翻倍。' },
    { id: 15, charId: 'defect', name: '万物一心', cost: 2, rarity: 'rare', type: '攻击', description: '造成 10 点伤害。将弃牌堆中所有 0 费牌放入手牌。', combo: '0费流的核心，可以循环打出海量的【爪击】。' },
    { id: 16, charId: 'defect', name: '核心激荡', cost: 1, rarity: 'rare', type: '攻击', description: '造成 11 点伤害。获得 1 层人工制品。', combo: '配合【偏差认知】可以抵消负面效果，永久保留集中力。' },
    { id: 27, charId: 'defect', name: '电击', cost: 1, rarity: 'common', type: '攻击', description: '造成 7 点伤害。生成 1 个闪电充能球。', combo: '最基础的产球手段。' },
    { id: 28, charId: 'defect', name: '偏差认知', cost: 1, rarity: 'rare', type: '能力', description: '获得 4(5) 点集中。每回合失去 1 点集中。', combo: '短期内爆发极强，配合【人工制品】或【橙色药丸】可消除负面效果。' },
    { id: 17, charId: 'primarch', name: '王之审判', cost: 2, rarity: 'uncommon', type: '攻击', description: '造成 15 点伤害。如果你处于进攻姿态，伤害翻倍。', combo: '核心爆发手段，在姿态切换后打出巨额伤害。' },
    { id: 18, charId: 'primarch', name: '威严光环', cost: 1, rarity: 'rare', type: '能力', description: '每回合开始时，获得 1 点威严。威严可以增加所有攻击的伤害。', combo: '长期成长的关键，威严层数越高，战斗力越强。' },
    { id: 19, charId: 'primarch', name: '流云势', cost: 0, rarity: 'common', type: '技能', description: '进入防御姿态。抽 1 张牌。', combo: '低费转防手段，同时保证手牌周转。' },
    { id: 20, charId: 'primarch', name: '御剑：归宗', cost: 3, rarity: 'rare', type: '攻击', description: '对随机敌人造成 7 点伤害 6 次。消耗所有威严，每消耗 1 点增加 1 次打击。', combo: '储君的终极终结技，在威严叠高后拥有秒杀 Boss 的潜力。' },
    { id: 29, charId: 'primarch', name: '皇室御盾', cost: 1, rarity: 'common', type: '技能', description: '获得 9 点格挡。如果你处于防御姿态，格挡值增加 50%。', combo: '姿态联动的防御牌。' },
    { id: 30, charId: 'primarch', name: '圣剑降临', cost: 2, rarity: 'rare', type: '攻击', description: '造成 20 点伤害。进入进攻姿态。', combo: '强力的姿态转换攻击，开启进攻回合。' },
  ],
  relics: [
    { id: 'burning_blood', name: '燃烧之血', rarity: 'starter', description: '在战斗结束时，回复 6 点生命值。', charId: 'ironclad', icon: 'fa-droplet' },
    { id: 'ring_of_the_snake', name: '蛇之戒指', rarity: 'starter', description: '在每场战斗开始时，额外抽 2 张牌。', charId: 'silent', icon: 'fa-ring' },
    { id: 'necro_book', name: '死灵书', rarity: 'event', description: '每回合你打出的第一张耗能为 2 或以上的攻击牌将额外打出一次。', charId: 'all', icon: 'fa-book-dead' },
    { id: 'shuriken', name: '手里剑', rarity: 'uncommon', description: '在同一回合内每打出 3 张攻击牌，获得 1 点力量。', charId: 'all', icon: 'fa-star' },
    { id: 'kunai', name: '苦无', rarity: 'uncommon', description: '在同一回合内每打出 3 张攻击牌，获得 1 点敏捷。', charId: 'all', icon: 'fa-caret-up' },
    { id: 'dead_branch', name: '枯木树枝', rarity: 'rare', description: '打出耗尽牌时，随机将一张牌加入手牌。', charId: 'all', icon: 'fa-tree' },
    { id: 'ice_cream', name: '冰淇淋', rarity: 'rare', description: '每回合未使用的费用可以保留至下一回合。', charId: 'all', icon: 'fa-ice-cream' },
    { id: 'mummified_hand', name: '木乃伊之手', rarity: 'uncommon', description: '使用一张能力牌后，本回合所有手牌费用 - 1。', charId: 'all', icon: 'fa-hand-dots' },
    { id: 'tungsten_rod', name: '钨钢棒', rarity: 'rare', description: '受到的所有伤害永久减少 1 点。', charId: 'all', icon: 'fa-shield-halved' },
  ],
  potions: [
    { id: 'fire_potion', name: '火焰药水', rarity: 'common', description: '造成 20 点伤害。', icon: 'fa-flask' },
    { id: 'strength_potion', name: '力量药水', rarity: 'common', description: '获得 2 点力量。', icon: 'fa-flask' },
    { id: 'dexterity_potion', name: '敏捷药水', rarity: 'common', description: '获得 2 点敏捷。', icon: 'fa-flask' },
    { id: 'distilled_chaos', name: '蒸馏混沌', rarity: 'uncommon', description: '从抽牌堆顶部开始打出 3 张牌。', icon: 'fa-flask-vial' },
    { id: 'energy_potion', name: '能量药水', rarity: 'common', description: '获得 2 点能量。', icon: 'fa-bolt' },
    { id: 'duplication_potion', name: '复制药水', rarity: 'uncommon', description: '本回合你打出的下一张牌将额外打出一次。', icon: 'fa-clone' },
    { id: 'fairy_in_a_bottle', name: '瓶中精灵', rarity: 'rare', description: '当你受到致命伤害时，不会死亡，而是回复至 30% 生命值并消耗此药水。', icon: 'fa-prescription-bottle' },
    { id: 'entropic_brew', name: '混沌药水', rarity: 'rare', description: '将你所有的药水栏位填满随机药水。', icon: 'fa-wine-bottle' },
  ],
  posts: [
    { id: 1, title: '杀戮尖塔2新手入门指南：从零开始征服塔楼', author: '尖塔老司机', date: '2026-05-15', category: '基础教学', summary: '本文将为您详细介绍《杀戮尖塔2》的基础机制、UI变动以及新手最容易犯的几个错误。', content: '在《杀戮尖塔2》中，资源管理比以往任何时候都更加重要。首先，你需要了解新增加的...', icon: 'fa-graduation-cap' },
    { id: 2, title: '死灵术师深度评测：为什么她是目前最强角色？', author: '卡牌大师', date: '2026-05-20', category: '进阶攻略', summary: '死灵术师的召唤系统带来了全新的维度，通过牺牲骷髅获取的收益远超想象。', content: '死灵术师的核心在于其灵魂能量（Soul Energy）的获取与消耗平衡。在本文中...', icon: 'fa-fire' },
    { id: 3, title: '全遗物强度排名：看到这些遗物请闭眼拿', author: '遗物猎人', date: '2026-05-25', category: '数据分析', summary: '基于上千场对局数据，我们总结出了当前版本最强力的几个核心遗物。', content: 'T0级别的遗物依然是那些能够提供额外能量或者强力抽牌效果的物品，例如...', icon: 'fa-list-ol' },
  ],
}

// 帖子弹窗详情（对应原生站 openModal 的 specificContent）
export const POST_DETAILS: Record<number, string> = {
  1: `<p class="text-xl leading-relaxed">新手开局优先选择 <strong class="text-red-500">战士</strong> 上手，卡组简单、生存能力强，更容易熟悉玩法。游戏核心是 <strong class="text-red-400">精简卡组、把控血线、合理选遗物</strong>，切忌盲目拿牌。</p>
<div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700 mt-4">
  <h4 class="text-lg font-bold mb-3 text-blue-400 flex items-center gap-2"><i class="fas fa-shield-alt"></i> 战斗与卡组建议</h4>
  <p class="mb-4">战斗初期以稳防御为主，别一味堆攻击，用格挡稳住血量，避免前期掉血过多。卡牌优先挑选低费实用牌，多余杂牌尽量在商店处删除，卡组越精简，抽牌稳定性越高。</p>
  <p>遗物优先选取加费用、减伤害、提升容错的类型，大幅提升通关概率。</p>
</div>
<div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700 mt-4">
  <h4 class="text-lg font-bold mb-3 text-orange-400 flex items-center gap-2"><i class="fas fa-map-marked-alt"></i> 路线与探索规划</h4>
  <p class="mb-4">每层探索优先走问号与篝火，篝火优先回血、删牌，少点升级。遇到精英怪量力而行，前期实力不足可绕开。打 BOSS 前务必补满状态，规划好出牌顺序。</p>
</div>
<div class="bg-slate-950 p-6 rounded-2xl border border-red-900/30 italic text-slate-400 mt-4">循序渐进熟悉各卡牌联动与流派思路，先尝试基础力量流、防御流，熟练后再探索进阶体系。多复盘失败对局，慢慢掌握节奏，就能稳步通关塔楼。</div>`,
  2: `<p class="text-xl leading-relaxed"><strong class="text-purple-500">死灵术师（Necrobinder）</strong> 是《杀戮尖塔 2》当前版本综合强度天花板，凭借独特机制与高上限构筑稳居 <strong class="text-red-500">T0 梯队</strong>。</p>
<div class="grid md:grid-cols-3 gap-4 mt-4">
  <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30"><h5 class="font-bold text-purple-400 mb-2">灾厄 (Doom)</h5><p class="text-xs text-slate-400">无视格挡直接斩杀，叠满即秒杀，上限远超中毒体系。</p></div>
  <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30"><h5 class="font-bold text-purple-400 mb-2">奥斯提 (Osty)</h5><p class="text-xs text-slate-400">召唤物替本体承伤，解决自身低血量短板，攻防一体。</p></div>
  <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30"><h5 class="font-bold text-purple-400 mb-2">灵魂系统</h5><p class="text-xs text-slate-400">提供 0 费过牌与减费，卡组运转极快，易形成无限循环。</p></div>
</div>
<div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700 mt-4"><h4 class="text-lg font-bold mb-4 text-slate-100">流派选择与核心卡</h4>
  <ul class="space-y-3 text-sm">
    <li class="flex items-start gap-2"><i class="fas fa-bolt text-yellow-500 mt-1"></i><span><strong>灾厄流：</strong> 启动快、斩杀稳，适合应对高甲敌人。</span></li>
    <li class="flex items-start gap-2"><i class="fas fa-ghost text-blue-400 mt-1"></i><span><strong>召唤流：</strong> 容错高、后期强势，通过海量召唤物淹没对手。</span></li>
    <li class="flex items-start gap-2"><i class="fas fa-magic text-purple-400 mt-1"></i><span><strong>灵魂流：</strong> 上限极高，适配多种遗物，追求极致的单回合爆发。</span></li>
  </ul>
  <div class="mt-6 p-4 bg-purple-950/30 rounded-lg border-l-4 border-purple-500"><p><strong>核心卡推荐：【挽歌】</strong>—— 兼具过牌与强化召唤，成型后几乎无解。</p></div>
</div>
<p class="text-slate-300 italic mt-4">虽前期本体脆弱、怕暴毙，但成型后输出无解、生存稳定、运转丝滑，适配所有难度，是当前版本最值得练的最强角色。</p>`,
  3: `<p class="mb-6">基于上千场对局数据，我们总结出了《杀戮尖塔2》当前版本的全遗物强度排名。本排名旨在为玩家提供选取参考，具体优先级仍需根据你的当前卡组构筑决定。</p>
<section><h4 class="text-2xl font-black text-red-500 mb-4 border-b border-red-900/50 pb-2">SS 级（超模遗物）</h4>
  <div class="grid gap-4">
    <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500"><p><strong class="text-red-400">枯木树枝：</strong>打出耗尽牌时，随机将一张牌加入手牌。无限流、烧牌流核心，泛用性极强。</p></div>
    <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500"><p><strong class="text-red-400">冰淇淋：</strong>每回合未使用的费用可以保留至下一回合。全职业通用顶级续航，彻底解决卡费问题。</p></div>
    <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500"><p><strong class="text-red-400">木乃伊之手：</strong>使用一张能力牌后，本回合所有手牌费用 - 1。能力流、无限流核心启动件。</p></div>
    <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500"><p><strong class="text-red-400">准备背包：</strong>每局战斗首回合额外抽 2 张牌。稳定起手，全流派通用。</p></div>
    <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500"><p><strong class="text-red-400">赌徒筹码：</strong>每局战斗开局可免费重抽一次手牌。规避起手崩盘，容错拉满。</p></div>
  </div>
</section>
<section class="mt-6"><h4 class="text-2xl font-black text-orange-500 mb-4 border-b border-orange-900/50 pb-2">S 级（强度顶尖）</h4>
  <div class="grid gap-3">
    <p><strong>钨钢棒：</strong>受到的所有伤害永久减少 1 点。最强常驻减伤，全程稳血线。</p>
    <p><strong>纸青蛙：</strong>施加的易伤效果额外提升 50% 伤害。爆发流通用核心。</p>
    <p><strong>灯笼：</strong>每局战斗首回合获得 1 点额外费用。前期节奏神器，加快启动。</p>
    <p><strong>卡戎之灰：</strong>每打出一张耗尽牌，对所有敌人造成 3 点伤害。烧牌流清场核心。</p>
    <p><strong>符文圆顶：</strong>免疫虚弱、易伤、中毒效果。对抗负面体系的防御神物。</p>
    <p><strong>灵体外质：</strong>永久增加 1 点最大费用，几乎无负面。顶级 BOSS 遗物。</p>
  </div>
</section>
<section class="mt-6"><h4 class="text-xl font-bold text-yellow-500 mb-4">A 级（实力强劲）</h4>
  <p class="text-slate-400 text-sm leading-relaxed">装饰折扇、蜥蜴尾巴、怀表、毒蛋、冠军腰带、冻结之眼。这些遗物适配多数主流卡组，单卡收益非常可观。</p>
</section>
<section class="bg-slate-950 p-6 rounded-2xl border border-slate-800 mt-6"><h4 class="text-lg font-bold text-slate-100 mb-4 flex items-center gap-2"><i class="fas fa-bullseye text-red-500"></i> 分角色简易选取参考</h4>
  <div class="grid md:grid-cols-2 gap-6 text-sm">
    <div><strong class="text-red-500">战士：</strong> 优先钨钢棒、灯笼、纸青蛙、熔岩蛋。</div>
    <div><strong class="text-green-500">静默猎手：</strong> 优先毒蛋、纸青蛙、苦无。</div>
    <div><strong class="text-blue-500">机器人：</strong> 优先冰淇淋、木乃伊之手、枯木树枝。</div>
    <div><strong class="text-amber-500">观者：</strong> 优先怀表、冻结之眼、百年拼图。</div>
  </div>
</section>`,
}
