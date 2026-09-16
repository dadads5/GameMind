<script setup lang="ts">
import { reactive, computed } from 'vue'

const form = reactive({
  baseAtk: 800,
  atkPercent: 0,
  flatAtk: 300,
  skillMultiplier: 200,
  flatDmg: 0,
  dmgBonus: 80,
  critRate: 70,
  critDmg: 180,
  charLvl: 90,
  enemyLvl: 90,
  baseRes: 10,
  resShred: 0,
  em: 0,
  reactionBonus: 0,
  reactionType: 'none' as 'none' | 'vaporize1' | 'vaporize2' | 'melt1' | 'melt2',
})

const reactionMap: Record<string, number> = {
  none: 1,
  vaporize1: 1.5,
  vaporize2: 2.0,
  melt1: 1.5,
  melt2: 2.0,
}

const result = computed(() => {
  const totalAtk = form.baseAtk * (1 + form.atkPercent / 100) + form.flatAtk
  const baseDmg = (totalAtk * form.skillMultiplier / 100 + form.flatDmg) * (1 + form.dmgBonus / 100)
  const critRateC = Math.min(form.critRate / 100, 1)
  const nonCrit = baseDmg
  const critVal = baseDmg * (1 + form.critDmg / 100)
  const avg = baseDmg * (1 + critRateC * form.critDmg / 100)
  const defMult = (form.charLvl + 100) / (form.charLvl + 100 + (form.enemyLvl + 200))
  const finalRes = form.baseRes / 100 - form.resShred / 100
  const resMult = finalRes < 0 ? 1 - finalRes / 2 : finalRes < 0.75 ? 1 - finalRes : 1 / (1 + finalRes * 4)
  const rm = reactionMap[form.reactionType] ?? 1
  const emBonus = (2.78 * form.em) / (form.em + 1400)
  const totalReactionBonus = rm * (1 + emBonus + form.reactionBonus / 100)
  const finalNonCrit = nonCrit * defMult * resMult
  const finalCrit = critVal * defMult * resMult
  const finalAvg = avg * defMult * resMult
  const finalReaction = finalCrit * totalReactionBonus
  const fmt = (n: number) => Math.round(n).toLocaleString()
  return {
    totalAtk: fmt(totalAtk),
    nonCrit: fmt(finalNonCrit),
    crit: fmt(finalCrit),
    avg: fmt(finalAvg),
    reaction: form.reactionType === 'none' ? null : fmt(finalReaction),
  }
})

const reset = () => {
  form.baseAtk = 800
  form.atkPercent = 0
  form.flatAtk = 300
  form.skillMultiplier = 200
  form.flatDmg = 0
  form.dmgBonus = 80
  form.critRate = 70
  form.critDmg = 180
  form.charLvl = 90
  form.enemyLvl = 90
  form.baseRes = 10
  form.resShred = 0
  form.em = 0
  form.reactionBonus = 0
  form.reactionType = 'none'
}

const numberFields = [
  { key: 'baseAtk', label: '基础攻击', step: 10 },
  { key: 'atkPercent', label: '攻击%', step: 1 },
  { key: 'flatAtk', label: '固定攻击', step: 10 },
  { key: 'skillMultiplier', label: '技能倍率%', step: 10 },
  { key: 'flatDmg', label: '固定伤害', step: 10 },
  { key: 'dmgBonus', label: '伤害加成%', step: 1 },
  { key: 'critRate', label: '暴击率%', step: 1 },
  { key: 'critDmg', label: '暴击伤害%', step: 1 },
  { key: 'charLvl', label: '角色等级', step: 1 },
  { key: 'enemyLvl', label: '敌人等级', step: 1 },
  { key: 'baseRes', label: '怪物抗性%', step: 1 },
  { key: 'resShred', label: '减抗%', step: 1 },
  { key: 'em', label: '元素精通', step: 10 },
  { key: 'reactionBonus', label: '反应伤害提高%', step: 1 },
] as const
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-950 via-indigo-950/30 to-slate-950 text-slate-100">
    <div class="max-w-5xl mx-auto px-4 py-8">
      <button @click="$router.back()" class="text-slate-400 hover:text-white mb-4">← 返回</button>
      <h1 class="text-2xl font-bold mb-6 bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
        伤害计算器
      </h1>

      <div class="grid md:grid-cols-2 gap-6">
        <!-- 输入 -->
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 space-y-3">
          <div v-for="f in numberFields" :key="f.key" class="flex items-center justify-between gap-3">
            <label class="text-sm text-slate-400">{{ f.label }}</label>
            <input
              v-model.number="form[f.key]"
              type="number"
              :step="f.step"
              class="w-32 px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 text-right focus:outline-none focus:border-indigo-500"
            />
          </div>
          <div class="flex items-center justify-between gap-3">
            <label class="text-sm text-slate-400">元素反应</label>
            <select
              v-model="form.reactionType"
              class="w-32 px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 focus:outline-none focus:border-indigo-500"
            >
              <option value="none">无</option>
              <option value="vaporize1">蒸发(火打水)</option>
              <option value="vaporize2">蒸发(水打火)</option>
              <option value="melt1">融化(冰打火)</option>
              <option value="melt2">融化(火打冰)</option>
            </select>
          </div>
          <button
            @click="reset"
            class="w-full mt-2 px-4 py-2 rounded-full bg-slate-800 text-slate-300 hover:bg-slate-700"
          >
            重置
          </button>
        </div>

        <!-- 结果 -->
        <div class="bg-slate-900/60 border border-slate-800 rounded-2xl p-6">
          <h2 class="text-lg font-semibold mb-4">计算结果</h2>
          <div class="space-y-3">
            <div class="flex justify-between"><span class="text-slate-400">总攻击力</span><span class="font-bold text-indigo-300">{{ result.totalAtk }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">未暴击伤害</span><span class="font-bold">{{ result.nonCrit }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">暴击伤害</span><span class="font-bold text-pink-300">{{ result.crit }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">期望伤害</span><span class="font-bold text-purple-300">{{ result.avg }}</span></div>
            <div v-if="result.reaction !== null" class="flex justify-between">
              <span class="text-slate-400">增幅反应后</span><span class="font-bold text-amber-300">{{ result.reaction }}</span>
            </div>
          </div>
          <p class="text-xs text-slate-500 mt-4">公式为简化估算，实际伤害以游戏内为准。</p>
        </div>
      </div>
    </div>
  </div>
</template>
