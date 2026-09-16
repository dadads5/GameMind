<script setup lang="ts">
import { ref, reactive } from 'vue'

const POOLS = {
  char1: { name: '喧寂于心', up5: ['尼可'], up4: ['布伦妮', '雷泽', '菲谢尔'], type: 'character' as const },
  char2: { name: '不熄灭的火', up5: ['杜林'], up4: ['布伦妮', '雷泽', '菲谢尔'], type: 'character' as const },
  weapon: {
    name: '神铸赋形',
    up5: ['尘光七谕', '黑蚀'],
    up4: ['织月者的曙色', '钟剑', '幽夜华尔兹', '匣里灭辰', '流浪乐章'],
    type: 'weapon' as const,
    fateSystem: true,
  },
  standard: {
    name: '奔行世间',
    standard5: ['琴', '迪卢克', '刻晴', '莫娜', '七七', '提纳里', '迪希雅'],
    type: 'standard' as const,
  },
}

const COMMON_4_CHAR = [
  '安柏', '凯亚', '丽莎', '芭芭拉', '雷泽', '菲谢尔', '班尼特', '诺艾尔', '砂糖', '行秋',
  '重云', '香菱', '北斗', '凝光', '辛焱', '罗莎莉亚', '迪奥娜', '早柚', '托马', '九条裟罗',
  '五郎', '云堇', '鹿野院平藏', '久岐忍', '坎蒂丝', '柯莱', '珉敏', '多莉', '莱依拉', '瑶瑶',
  '绮良良', '卡齐娜', '夏洛蒂', '嘉明',
]
const COMMON_4_WEAPON = ['铁蜂刺', '祭礼剑', '西风剑', '笛剑', '暗铁剑', '宗室长剑', '钟剑', '白影剑', '西风大剑', '螭骨剑']
const COMMON_3 = ['弹弓', '神射手之誓', '鸦羽弓', '信使', '反曲弓', '翡玉法球', '魔导绪论', '讨龙英杰谭', '降临之柱', '铁影阔剑']

const currentPool = ref<keyof typeof POOLS>('char1')
const totalPulls = ref(0)
const history5 = ref<{ name: string; pity: number }[]>([])
const showResults = ref(false)
const results = ref<{ name: string; star: number }[]>([])

const poolData = reactive({
  char: { pity5: 0, pity4: 0, isGuaranteed: false },
  weapon: { pity5: 0, pity4: 0, isGuaranteed: false, fateValue: 0 },
  standard: { pity5: 0, pity4: 0 },
})

const rand = <T,>(arr: T[]): T => arr[Math.floor(Math.random() * arr.length)]

const pullOne = (poolType: 'char' | 'weapon' | 'standard') => {
  const pData = poolData[poolType]
  pData.pity5++
  pData.pity4++
  totalPulls.value++

  let probability5 = 0.006
  if (pData.pity5 >= 73) probability5 += (pData.pity5 - 72) * 0.06
  if (pData.pity5 >= 90) probability5 = 1
  let probability4 = 0.051
  if (pData.pity4 >= 10) probability4 = 1

  const r = Math.random()
  let star = 3
  if (r <= probability5) {
    star = 5
    pData.pity5 = 0
    pData.pity4 = 0
  } else if (r <= probability5 + probability4) {
    star = 4
    pData.pity4 = 0
  }

  const pool = POOLS[currentPool.value]
  let name = ''
  if (star === 5) {
    if (poolType === 'standard') {
      name = rand(pool.standard5)
    } else {
      const guaranteed = (pData as any).isGuaranteed || Math.random() < 0.5
      if (guaranteed) {
        name = rand(pool.up5)
        ;(pData as any).isGuaranteed = false
      } else {
        const nonUp = pool.standard5.filter((n) => !pool.up5.includes(n))
        name = rand(nonUp.length ? nonUp : pool.standard5)
        ;(pData as any).isGuaranteed = true
      }
      if (poolType === 'weapon' && pool.fateSystem) {
        if (pool.up5.includes(name)) {
          ;(pData as any).fateValue = 0
        } else {
          ;(pData as any).fateValue++
          if ((pData as any).fateValue >= 2) {
            name = rand(pool.up5)
            ;(pData as any).fateValue = 0
            ;(pData as any).isGuaranteed = false
          }
        }
      }
    }
    history5.value.unshift({ name, pity: pData.pity5 })
    if (history5.value.length > 20) history5.value.pop()
  } else if (star === 4) {
    const isUp4 = Math.random() < 0.5 && pool.up4
    name = isUp4 ? rand(pool.up4) : rand([...COMMON_4_CHAR, ...COMMON_4_WEAPON])
  } else {
    name = rand(COMMON_3)
  }
  return { name, star }
}

const doWish = (times: number) => {
  const poolType = POOLS[currentPool.value].type === 'standard' ? 'standard' : POOLS[currentPool.value].type === 'weapon' ? 'weapon' : 'char'
  const res: { name: string; star: number }[] = []
  for (let i = 0; i < times; i++) res.push(pullOne(poolType as any))
  results.value = res
  showResults.value = true
}

const switchBanner = (key: keyof typeof POOLS) => {
  currentPool.value = key
}

const activePool = () => POOLS[currentPool.value]
const pityInfo = () => {
  const pd = poolData[POOLS[currentPool.value].type === 'standard' ? 'standard' : POOLS[currentPool.value].type === 'weapon' ? 'weapon' : 'char']
  return {
    p5: 90 - pd.pity5,
    p4: 10 - pd.pity4,
    guaranteed: (pd as any).isGuaranteed ? '是' : '否',
  }
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/30 to-slate-950 text-slate-100">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <button @click="$router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6 bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
        抽卡模拟器
      </h1>

      <!-- 卡池切换 -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="(p, key) in POOLS"
          :key="key"
          @click="switchBanner(key as keyof typeof POOLS)"
          :class="currentPool === key ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-300'"
          class="px-3 py-1.5 rounded-full text-sm transition"
        >
          {{ p.name }}
        </button>
      </div>

      <!-- 当前卡池 -->
      <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 mb-6">
        <h2 class="text-lg font-semibold mb-2">{{ activePool().name }}</h2>
        <p class="text-slate-400 text-sm mb-1">UP 五星：{{ activePool().up5?.join('、') || activePool().standard5?.join('、') }}</p>
        <p v-if="activePool().up4" class="text-slate-400 text-sm">UP 四星：{{ activePool().up4.join('、') }}</p>
      </div>

      <!-- 保底信息 -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-4 text-center">
          <div class="text-xl font-bold text-indigo-300">{{ pityInfo().p5 }}</div>
          <div class="text-slate-400 text-xs">距五星保底</div>
        </div>
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-4 text-center">
          <div class="text-xl font-bold text-purple-300">{{ pityInfo().p4 }}</div>
          <div class="text-slate-400 text-xs">距四星保底</div>
        </div>
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-4 text-center">
          <div class="text-xl font-bold text-amber-300">{{ pityInfo().guaranteed }}</div>
          <div class="text-slate-400 text-xs">大保底</div>
        </div>
      </div>

      <!-- 操作 -->
      <div class="flex gap-3 mb-6">
        <button @click="doWish(1)" class="flex-1 py-3 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-medium hover:shadow-lg hover:shadow-indigo-500/30 transition">
          祈愿 ×1
        </button>
        <button @click="doWish(10)" class="flex-1 py-3 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 text-white font-medium hover:shadow-lg hover:shadow-purple-500/30 transition">
          祈愿 ×10
        </button>
      </div>

      <!-- 五星历史 -->
      <h3 class="text-lg font-semibold mb-3">五星记录（共 {{ totalPulls }} 抽）</h3>
      <div class="space-y-2">
        <div
          v-for="(h, i) in history5"
          :key="i"
          class="bg-slate-900/40 border border-slate-800 rounded-xl px-4 py-2 flex justify-between"
        >
          <span class="text-amber-300 font-medium">{{ h.name }}</span>
          <span class="text-slate-500 text-sm">第 {{ h.pity }} 抽</span>
        </div>
        <div v-if="history5.length === 0" class="text-slate-500 text-sm">还没有抽到五星，继续加油！</div>
      </div>
    </div>

    <!-- 结果弹窗 -->
    <div
      v-if="showResults"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4"
      @click.self="showResults = false"
    >
      <div class="bg-slate-900 border border-slate-700 rounded-2xl p-6 w-full max-w-lg max-h-[80vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-4 text-center">祈愿结果</h3>
        <div class="grid grid-cols-5 gap-2">
          <div
            v-for="(r, i) in results"
            :key="i"
            class="aspect-square rounded-lg flex items-center justify-center text-center p-1 text-xs"
            :class="{
              'bg-amber-500/20 text-amber-300': r.star === 5,
              'bg-purple-500/20 text-purple-300': r.star === 4,
              'bg-slate-700/40 text-slate-400': r.star === 3,
            }"
          >
            {{ r.name }}
          </div>
        </div>
        <div class="flex justify-center mt-5">
          <button @click="showResults = false" class="px-6 py-2 rounded-full bg-indigo-500 text-white">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>
