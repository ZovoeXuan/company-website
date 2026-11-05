<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { closeRoiCalculator, openConsult, useRoiModal } from '@/composables/consult'

const { roiModal } = useRoiModal()

interface IndustryPreset {
  key: string
  label: string
  unitLabel: string
  defaultUnits: number
  defaultMonthlyCost: number
  fuelSavingRate: number
  safetyReductionRate: number
  efficiencyGainRate: number
  highlight: string
}

const INDUSTRIES: IndustryPreset[] = [
  {
    key: 'logistics',
    label: '大宗能源与干线物流',
    unitLabel: '自有及协同运力规模（辆）',
    defaultUnits: 120,
    defaultMonthlyCost: 18000,
    fuelSavingRate: 0.075,
    safetyReductionRate: 68,
    efficiencyGainRate: 45,
    highlight: 'AI 视频防偷油 + 主动安全风控 + 智能排线调度',
  },
  {
    key: 'coldchain',
    label: '医药与生鲜冷链物流',
    unitLabel: '冷链温控车及冷库节点（台/个）',
    defaultUnits: 80,
    defaultMonthlyCost: 22000,
    fuelSavingRate: 0.088,
    safetyReductionRate: 74,
    efficiencyGainRate: 52,
    highlight: '全链路温湿度毫秒级预警 + GSP 合规自动留档 + 货损率骤降',
  },
  {
    key: 'hazmat',
    label: '危化品与特种运输',
    unitLabel: '受控危化运输车辆（辆）',
    defaultUnits: 60,
    defaultMonthlyCost: 26000,
    fuelSavingRate: 0.082,
    safetyReductionRate: 82,
    efficiencyGainRate: 40,
    highlight: 'DMS 疲劳干预 + 电子围栏合规联锁 + 7×24h 安全管家托管',
  },
  {
    key: 'retail',
    label: '快消与连锁零售城配',
    unitLabel: '城配车辆与门店配送线路（条/辆）',
    defaultUnits: 150,
    defaultMonthlyCost: 12000,
    fuelSavingRate: 0.092,
    safetyReductionRate: 60,
    efficiencyGainRate: 58,
    highlight: 'AI 智能拼单排线 + 门店准点率看板 + 装载率动态优化',
  },
  {
    key: 'manufacturing',
    label: '制造厂区与供应链协同',
    unitLabel: '厂内物流设备与协同承运商节点（个）',
    defaultUnits: 100,
    defaultMonthlyCost: 15000,
    fuelSavingRate: 0.07,
    safetyReductionRate: 65,
    efficiencyGainRate: 48,
    highlight: '月台智能预约排队 + 在途可视化追踪 + 自动对账结算',
  },
]

const state = reactive({
  industryKey: 'logistics',
  units: 120,
  monthlyCostPerUnit: 18000,
  dispatchStaff: 8,
})

const currentIndustry = computed(
  () => INDUSTRIES.find((item) => item.key === state.industryKey) ?? INDUSTRIES[0],
)

function selectIndustry(item: IndustryPreset) {
  state.industryKey = item.key
  state.units = item.defaultUnits
  state.monthlyCostPerUnit = item.defaultMonthlyCost
}

watch(
  () => roiModal.presetIndustry,
  (val) => {
    const found = INDUSTRIES.find((i) => i.key === val)
    if (found) selectIndustry(found)
  },
)

// 计算年度节省金额（万元）
const annualDirectSavingWan = computed(() => {
  const raw = state.units * state.monthlyCostPerUnit * 12 * currentIndustry.value.fuelSavingRate
  return Math.max(5, Math.round((raw / 10000) * 10) / 10)
})

const annualLaborSavingWan = computed(() => {
  const raw = state.dispatchStaff * 9500 * 12 * (currentIndustry.value.efficiencyGainRate / 100) * 0.65
  return Math.max(2, Math.round((raw / 10000) * 10) / 10)
})

const totalAnnualSavingWan = computed(() => {
  return (annualDirectSavingWan.value + annualLaborSavingWan.value).toFixed(1)
})

const paybackMonths = computed(() => {
  const baseMonths = 4.2 - Math.min(2.0, state.units / 400)
  return Math.max(1.8, Math.round(baseMonths * 10) / 10)
})

function requestReport() {
  const summary = `【ROI 效益测算报告预约】行业场景：${currentIndustry.value.label}；资产/运力规模：${state.units}；单节点月均成本：¥${state.monthlyCostPerUnit}；调度团队：${state.dispatchStaff}人。系统测算预计年化综合降本约 ${totalAnnualSavingWan.value} 万元/年（预计回本周期 ${paybackMonths.value} 个月）。希望获取完整 PDF 测算报告与专家落地方案。`
  closeRoiCalculator()
  openConsult({
    sourcePosition: 'OTHER_CTA',
    title: '获取专属 ROI 测算报告与落地方案',
    initialDemand: summary,
  })
}
</script>

<template>
  <Teleport to="body">
    <div v-if="roiModal.open" class="roi-overlay" @click.self="closeRoiCalculator">
      <div class="roi-dialog" role="dialog" aria-modal="true" aria-labelledby="roi-title">
        <div class="roi-dialog__head">
          <div>
            <span class="roi-kicker">INTERACTIVE ROI SIMULATOR · 数字化效益精算模型</span>
            <h3 id="roi-title" class="roi-title">企业数字化与 AI 调度降本增效测算器</h3>
          </div>
          <button type="button" class="roi-close" aria-label="关闭测算器" @click="closeRoiCalculator">✕</button>
        </div>

        <!-- 行业切换 -->
        <div class="roi-tabs">
          <button
            v-for="item in INDUSTRIES"
            :key="item.key"
            type="button"
            class="roi-tab"
            :class="{ 'is-active': state.industryKey === item.key }"
            @click="selectIndustry(item)"
          >
            {{ item.label }}
          </button>
        </div>

        <div class="roi-body">
          <!-- 左侧参数输入 -->
          <div class="roi-inputs">
            <div class="roi-field">
              <div class="roi-field__head">
                <label>{{ currentIndustry.unitLabel }}</label>
                <span class="roi-field__val">{{ state.units }}</span>
              </div>
              <input v-model.number="state.units" type="range" min="10" max="2000" step="10" class="roi-slider" />
              <div class="roi-field__scale">
                <span>10</span>
                <span>500</span>
                <span>1000</span>
                <span>2000+</span>
              </div>
            </div>

            <div class="roi-field">
              <div class="roi-field__head">
                <label>单节点月均能耗/货损/运营成本（元）</label>
                <span class="roi-field__val">¥ {{ state.monthlyCostPerUnit.toLocaleString() }}</span>
              </div>
              <input
                v-model.number="state.monthlyCostPerUnit"
                type="range"
                min="3000"
                max="50000"
                step="1000"
                class="roi-slider"
              />
              <div class="roi-field__scale">
                <span>¥3,000</span>
                <span>¥15,000</span>
                <span>¥30,000</span>
                <span>¥50,000</span>
              </div>
            </div>

            <div class="roi-field">
              <div class="roi-field__head">
                <label>安全与调度运营人员规模（人）</label>
                <span class="roi-field__val">{{ state.dispatchStaff }} 人</span>
              </div>
              <input v-model.number="state.dispatchStaff" type="range" min="1" max="100" step="1" class="roi-slider" />
              <div class="roi-field__scale">
                <span>1 人</span>
                <span>25 人</span>
                <span>50 人</span>
                <span>100 人</span>
              </div>
            </div>

            <div class="roi-highlight">
              <span class="roi-highlight__tag">核心赋能路径</span>
              <span>{{ currentIndustry.highlight }}</span>
            </div>
          </div>

          <!-- 右侧实时测算看板 -->
          <div class="roi-result">
            <div class="roi-result__hero">
              <span class="roi-result__label">预计年化综合节省成本</span>
              <div class="roi-result__number">
                <span class="roi-result__currency">¥</span>
                <strong>{{ totalAnnualSavingWan }}</strong>
                <span class="roi-result__unit">万元 / 年</span>
              </div>
              <p class="roi-result__breakdown">
                含能耗与货损直接节降约 <b>{{ annualDirectSavingWan }} 万</b> + 调度与合规人效释放约
                <b>{{ annualLaborSavingWan }} 万</b>
              </p>
            </div>

            <div class="roi-metrics-grid">
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ currentIndustry.safetyReductionRate }}% ↓</span>
                <span class="roi-metric-box__lbl">高危风险与事故率下降</span>
              </div>
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ currentIndustry.efficiencyGainRate }}% ↑</span>
                <span class="roi-metric-box__lbl">智能排线与调度人效提升</span>
              </div>
              <div class="roi-metric-box">
                <span class="roi-metric-box__val">{{ paybackMonths }} 个月</span>
                <span class="roi-metric-box__lbl">预计数字化投资回本周期</span>
              </div>
            </div>

            <button type="button" class="roi-cta-btn" @click="requestReport">
              <span>获取完整 PDF 测算报告与专家定制方案</span>
              <span aria-hidden="true">→</span>
            </button>
            <p class="roi-disclaimer">
              * 基于同行业标杆客户历史交付中位数建模测算，点击上方按钮可一键将测算参数同步给顾问专家。
            </p>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.roi-overlay {
  position: fixed;
  inset: 0;
  z-index: 85;
  background: rgba(8, 10, 18, 0.72);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.roi-dialog {
  width: min(920px, 96vw);
  max-height: 92dvh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 24px;
  padding: 32px 36px;
  box-shadow: 0 32px 80px rgba(8, 10, 20, 0.35);
}

.roi-dialog__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.roi-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: #5b4be8;
}

.roi-title {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #101014;
}

.roi-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(16, 16, 20, 0.12);
  background: #f5f7fa;
  color: #5f6470;
  font-size: 16px;
  cursor: pointer;
}

.roi-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 26px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(16, 16, 20, 0.08);
}

.roi-tab {
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid rgba(16, 16, 20, 0.12);
  background: #ffffff;
  color: #5f6470;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.roi-tab.is-active {
  background: #5b4be8;
  border-color: #5b4be8;
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(91, 75, 232, 0.25);
}

.roi-body {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 32px;
  align-items: stretch;
}

@media (max-width: 820px) {
  .roi-body {
    grid-template-columns: 1fr;
  }
  .roi-dialog {
    padding: 22px 20px;
  }
}

.roi-inputs {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.roi-field__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #101014;
}

.roi-field__val {
  color: #5b4be8;
  font-size: 16px;
  font-weight: 700;
}

.roi-slider {
  width: 100%;
  accent-color: #5b4be8;
  cursor: pointer;
}

.roi-field__scale {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #8b90a0;
  margin-top: 4px;
}

.roi-highlight {
  margin-top: auto;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f5f3ff;
  border: 1px solid rgba(91, 75, 232, 0.18);
  font-size: 13px;
  color: #3730a3;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.roi-highlight__tag {
  padding: 2px 8px;
  border-radius: 6px;
  background: #5b4be8;
  color: #ffffff;
  font-size: 11px;
  font-weight: 600;
}

.roi-result {
  background: linear-gradient(155deg, #0b0d16 0%, #151728 60%, #231c4c 100%);
  color: #ffffff;
  border-radius: 18px;
  padding: 26px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.roi-result__label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}

.roi-result__number {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 8px 0;
}

.roi-result__currency {
  font-size: 24px;
  font-weight: 700;
  color: #a5b4fc;
}

.roi-result__number strong {
  font-size: 46px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
  color: #ffffff;
}

.roi-result__unit {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
}

.roi-result__breakdown {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.68);
  line-height: 1.6;
  margin: 0 0 18px;
}

.roi-result__breakdown b {
  color: #c4b5fd;
}

.roi-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.roi-metric-box {
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 10px;
  text-align: center;
}

.roi-metric-box__val {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #38bdf8;
}

.roi-metric-box__lbl {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.68);
}

.roi-cta-btn {
  width: 100%;
  min-height: 48px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #6366f1 0%, #5b4be8 100%);
  color: #ffffff;
  font-size: 14.5px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 10px 24px rgba(91, 75, 232, 0.4);
}

.roi-cta-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(91, 75, 232, 0.55);
}

.roi-disclaimer {
  margin: 10px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  line-height: 1.5;
}
</style>
