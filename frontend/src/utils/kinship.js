const relationTokens = [
  {
    key: 'father',
    label: '爸爸',
    aliases: ['爸爸', '父亲', '老爸', '爸'],
    group: '直系长辈',
    direction: '向上一代',
    note: '沿父系向上一代寻找男性长辈。'
  },
  {
    key: 'mother',
    label: '妈妈',
    aliases: ['妈妈', '母亲', '老妈', '妈'],
    group: '直系长辈',
    direction: '向上一代',
    note: '沿母系向上一代寻找女性长辈。'
  },
  {
    key: 'olderBrother',
    label: '哥哥',
    aliases: ['哥哥'],
    group: '平辈关系',
    direction: '同辈',
    note: '在当前人物的平辈中寻找年长的男性。'
  },
  {
    key: 'youngerBrother',
    label: '弟弟',
    aliases: ['弟弟'],
    group: '平辈关系',
    direction: '同辈',
    note: '在当前人物的平辈中寻找年幼的男性。'
  },
  {
    key: 'olderSister',
    label: '姐姐',
    aliases: ['姐姐'],
    group: '平辈关系',
    direction: '同辈',
    note: '在当前人物的平辈中寻找年长的女性。'
  },
  {
    key: 'youngerSister',
    label: '妹妹',
    aliases: ['妹妹'],
    group: '平辈关系',
    direction: '同辈',
    note: '在当前人物的平辈中寻找年幼的女性。'
  },
  {
    key: 'son',
    label: '儿子',
    aliases: ['儿子'],
    group: '晚辈关系',
    direction: '向下一代',
    note: '沿直系向下一代寻找男性晚辈。'
  },
  {
    key: 'daughter',
    label: '女儿',
    aliases: ['女儿'],
    group: '晚辈关系',
    direction: '向下一代',
    note: '沿直系向下一代寻找女性晚辈。'
  },
  {
    key: 'husband',
    label: '丈夫',
    aliases: ['丈夫', '老公', '先生'],
    group: '配偶关系',
    direction: '配偶',
    note: '切换到当前人物的男性配偶。'
  },
  {
    key: 'wife',
    label: '妻子',
    aliases: ['妻子', '老婆', '夫人'],
    group: '配偶关系',
    direction: '配偶',
    note: '切换到当前人物的女性配偶。'
  },
  {
    key: 'paternalGrandfather',
    label: '爷爷',
    aliases: ['爷爷', '祖父'],
    group: '祖辈关系',
    direction: '向上两代',
    note: '定位到父亲的父亲。'
  },
  {
    key: 'paternalGrandmother',
    label: '奶奶',
    aliases: ['奶奶', '祖母'],
    group: '祖辈关系',
    direction: '向上两代',
    note: '定位到父亲的母亲。'
  },
  {
    key: 'maternalGrandfather',
    label: '外公',
    aliases: ['外公', '姥爷'],
    group: '祖辈关系',
    direction: '向上两代',
    note: '定位到母亲的父亲。'
  },
  {
    key: 'maternalGrandmother',
    label: '外婆',
    aliases: ['外婆', '姥姥'],
    group: '祖辈关系',
    direction: '向上两代',
    note: '定位到母亲的母亲。'
  },
  {
    key: 'olderPaternalUncle',
    label: '伯伯',
    aliases: ['伯伯', '伯父'],
    group: '旁系长辈',
    direction: '父系旁支',
    note: '定位到父亲年长的兄长。'
  },
  {
    key: 'youngerPaternalUncle',
    label: '叔叔',
    aliases: ['叔叔', '叔父'],
    group: '旁系长辈',
    direction: '父系旁支',
    note: '定位到父亲年幼的弟弟。'
  },
  {
    key: 'paternalAunt',
    label: '姑姑',
    aliases: ['姑姑', '姑妈', '姑母'],
    group: '旁系长辈',
    direction: '父系旁支',
    note: '定位到父亲的姐妹。'
  },
  {
    key: 'maternalUncle',
    label: '舅舅',
    aliases: ['舅舅', '舅父'],
    group: '旁系长辈',
    direction: '母系旁支',
    note: '定位到母亲的兄弟。'
  },
  {
    key: 'maternalAunt',
    label: '阿姨',
    aliases: ['阿姨', '姨妈', '姨母'],
    group: '旁系长辈',
    direction: '母系旁支',
    note: '定位到母亲的姐妹。'
  },
  {
    key: 'brotherSon',
    label: '侄子',
    aliases: ['侄子'],
    group: '旁系晚辈',
    direction: '兄弟下一代',
    note: '定位到兄弟的儿子。'
  },
  {
    key: 'brotherDaughter',
    label: '侄女',
    aliases: ['侄女'],
    group: '旁系晚辈',
    direction: '兄弟下一代',
    note: '定位到兄弟的女儿。'
  },
  {
    key: 'sisterSon',
    label: '外甥',
    aliases: ['外甥'],
    group: '旁系晚辈',
    direction: '姐妹下一代',
    note: '定位到姐妹的儿子。'
  },
  {
    key: 'sisterDaughter',
    label: '外甥女',
    aliases: ['外甥女'],
    group: '旁系晚辈',
    direction: '姐妹下一代',
    note: '定位到姐妹的女儿。'
  },
  {
    key: 'paternalOlderMaleCousin',
    label: '堂哥',
    aliases: ['堂哥'],
    group: '堂表亲',
    direction: '父系同辈',
    note: '定位到父系同辈中年长的男性堂亲。'
  },
  {
    key: 'paternalYoungerMaleCousin',
    label: '堂弟',
    aliases: ['堂弟'],
    group: '堂表亲',
    direction: '父系同辈',
    note: '定位到父系同辈中年幼的男性堂亲。'
  },
  {
    key: 'paternalOlderFemaleCousin',
    label: '堂姐',
    aliases: ['堂姐'],
    group: '堂表亲',
    direction: '父系同辈',
    note: '定位到父系同辈中年长的女性堂亲。'
  },
  {
    key: 'paternalYoungerFemaleCousin',
    label: '堂妹',
    aliases: ['堂妹'],
    group: '堂表亲',
    direction: '父系同辈',
    note: '定位到父系同辈中年幼的女性堂亲。'
  },
  {
    key: 'maternalOlderMaleCousin',
    label: '表哥',
    aliases: ['表哥'],
    group: '堂表亲',
    direction: '表亲',
    note: '定位到表亲中年长的男性。'
  },
  {
    key: 'maternalYoungerMaleCousin',
    label: '表弟',
    aliases: ['表弟'],
    group: '堂表亲',
    direction: '表亲',
    note: '定位到表亲中年幼的男性。'
  },
  {
    key: 'maternalOlderFemaleCousin',
    label: '表姐',
    aliases: ['表姐'],
    group: '堂表亲',
    direction: '表亲',
    note: '定位到表亲中年长的女性。'
  },
  {
    key: 'maternalYoungerFemaleCousin',
    label: '表妹',
    aliases: ['表妹'],
    group: '堂表亲',
    direction: '表亲',
    note: '定位到表亲中年幼的女性。'
  }
]

const stateMeta = {
  self: {
    title: '我',
    description: '关系起点',
    order: 0
  },
  father: {
    title: '爸爸',
    description: '我的父亲',
    order: 1
  },
  mother: {
    title: '妈妈',
    description: '我的母亲',
    order: 2
  },
  olderBrother: {
    title: '哥哥',
    description: '我的年长男性同辈',
    order: 3
  },
  youngerBrother: {
    title: '弟弟',
    description: '我的年幼男性同辈',
    order: 4
  },
  olderSister: {
    title: '姐姐',
    description: '我的年长女性同辈',
    order: 5
  },
  youngerSister: {
    title: '妹妹',
    description: '我的年幼女性同辈',
    order: 6
  },
  son: {
    title: '儿子',
    description: '我的男性晚辈',
    order: 7
  },
  daughter: {
    title: '女儿',
    description: '我的女性晚辈',
    order: 8
  },
  husband: {
    title: '丈夫',
    description: '我的男性配偶',
    order: 9
  },
  wife: {
    title: '妻子',
    description: '我的女性配偶',
    order: 10
  },
  paternalGrandfather: {
    title: '爷爷',
    description: '父亲的父亲',
    order: 11
  },
  paternalGrandmother: {
    title: '奶奶',
    description: '父亲的母亲',
    order: 12
  },
  maternalGrandfather: {
    title: '外公',
    description: '母亲的父亲',
    order: 13
  },
  maternalGrandmother: {
    title: '外婆',
    description: '母亲的母亲',
    order: 14
  },
  olderPaternalUncle: {
    title: '伯伯',
    description: '父亲的哥哥',
    order: 15
  },
  youngerPaternalUncle: {
    title: '叔叔',
    description: '父亲的弟弟',
    order: 16
  },
  paternalAunt: {
    title: '姑姑',
    description: '父亲的姐妹',
    order: 17
  },
  maternalUncle: {
    title: '舅舅',
    description: '母亲的兄弟',
    order: 18
  },
  maternalAunt: {
    title: '阿姨',
    description: '母亲的姐妹',
    order: 19
  },
  brotherSon: {
    title: '侄子',
    description: '兄弟的儿子',
    order: 20
  },
  brotherDaughter: {
    title: '侄女',
    description: '兄弟的女儿',
    order: 21
  },
  sisterSon: {
    title: '外甥',
    description: '姐妹的儿子',
    order: 22
  },
  sisterDaughter: {
    title: '外甥女',
    description: '姐妹的女儿',
    order: 23
  },
  paternalMaleCousinGeneric: {
    title: '堂兄弟',
    description: '父系同辈男性堂亲，未区分年长或年幼',
    order: 24
  },
  paternalFemaleCousinGeneric: {
    title: '堂姐妹',
    description: '父系同辈女性堂亲，未区分年长或年幼',
    order: 25
  },
  maternalMaleCousinGeneric: {
    title: '表兄弟',
    description: '表亲中的男性，未区分年长或年幼',
    order: 26
  },
  maternalFemaleCousinGeneric: {
    title: '表姐妹',
    description: '表亲中的女性，未区分年长或年幼',
    order: 27
  },
  paternalOlderMaleCousin: {
    title: '堂哥',
    description: '父系同辈中年长的男性堂亲',
    order: 28
  },
  paternalYoungerMaleCousin: {
    title: '堂弟',
    description: '父系同辈中年幼的男性堂亲',
    order: 29
  },
  paternalOlderFemaleCousin: {
    title: '堂姐',
    description: '父系同辈中年长的女性堂亲',
    order: 30
  },
  paternalYoungerFemaleCousin: {
    title: '堂妹',
    description: '父系同辈中年幼的女性堂亲',
    order: 31
  },
  maternalOlderMaleCousin: {
    title: '表哥',
    description: '表亲中年长的男性',
    order: 32
  },
  maternalYoungerMaleCousin: {
    title: '表弟',
    description: '表亲中年幼的男性',
    order: 33
  },
  maternalOlderFemaleCousin: {
    title: '表姐',
    description: '表亲中年长的女性',
    order: 34
  },
  maternalYoungerFemaleCousin: {
    title: '表妹',
    description: '表亲中年幼的女性',
    order: 35
  }
}

const directStateByToken = {
  father: 'father',
  mother: 'mother',
  olderBrother: 'olderBrother',
  youngerBrother: 'youngerBrother',
  olderSister: 'olderSister',
  youngerSister: 'youngerSister',
  son: 'son',
  daughter: 'daughter',
  husband: 'husband',
  wife: 'wife',
  paternalGrandfather: 'paternalGrandfather',
  paternalGrandmother: 'paternalGrandmother',
  maternalGrandfather: 'maternalGrandfather',
  maternalGrandmother: 'maternalGrandmother',
  olderPaternalUncle: 'olderPaternalUncle',
  youngerPaternalUncle: 'youngerPaternalUncle',
  paternalAunt: 'paternalAunt',
  maternalUncle: 'maternalUncle',
  maternalAunt: 'maternalAunt',
  brotherSon: 'brotherSon',
  brotherDaughter: 'brotherDaughter',
  sisterSon: 'sisterSon',
  sisterDaughter: 'sisterDaughter',
  paternalOlderMaleCousin: 'paternalOlderMaleCousin',
  paternalYoungerMaleCousin: 'paternalYoungerMaleCousin',
  paternalOlderFemaleCousin: 'paternalOlderFemaleCousin',
  paternalYoungerFemaleCousin: 'paternalYoungerFemaleCousin',
  maternalOlderMaleCousin: 'maternalOlderMaleCousin',
  maternalYoungerMaleCousin: 'maternalYoungerMaleCousin',
  maternalOlderFemaleCousin: 'maternalOlderFemaleCousin',
  maternalYoungerFemaleCousin: 'maternalYoungerFemaleCousin'
}

const tokenMetaMap = relationTokens.reduce((acc, item) => {
  acc[item.key] = item
  return acc
}, {})

export const relationTokenList = relationTokens
export const relationTokenMap = tokenMetaMap

const aliasLookup = relationTokens.reduce((acc, item) => {
  item.aliases.forEach(alias => {
    acc.set(alias, item.key)
  })
  return acc
}, new Map())

const selfTransitions = Object.entries(directStateByToken).reduce((acc, [tokenKey, stateKey]) => {
  acc[tokenKey] = [stateKey]
  return acc
}, {})

const transitions = {
  self: selfTransitions,
  father: {
    father: ['paternalGrandfather'],
    mother: ['paternalGrandmother'],
    wife: ['mother'],
    olderBrother: ['olderPaternalUncle'],
    youngerBrother: ['youngerPaternalUncle'],
    olderSister: ['paternalAunt'],
    youngerSister: ['paternalAunt'],
    son: ['self', 'olderBrother', 'youngerBrother'],
    daughter: ['self', 'olderSister', 'youngerSister']
  },
  mother: {
    father: ['maternalGrandfather'],
    mother: ['maternalGrandmother'],
    husband: ['father'],
    olderBrother: ['maternalUncle'],
    youngerBrother: ['maternalUncle'],
    olderSister: ['maternalAunt'],
    youngerSister: ['maternalAunt'],
    son: ['self', 'olderBrother', 'youngerBrother'],
    daughter: ['self', 'olderSister', 'youngerSister']
  },
  paternalGrandfather: {
    son: ['father', 'olderPaternalUncle', 'youngerPaternalUncle'],
    daughter: ['paternalAunt']
  },
  paternalGrandmother: {
    son: ['father', 'olderPaternalUncle', 'youngerPaternalUncle'],
    daughter: ['paternalAunt']
  },
  maternalGrandfather: {
    son: ['maternalUncle'],
    daughter: ['mother', 'maternalAunt']
  },
  maternalGrandmother: {
    son: ['maternalUncle'],
    daughter: ['mother', 'maternalAunt']
  },
  olderBrother: {
    father: ['father'],
    mother: ['mother'],
    son: ['brotherSon'],
    daughter: ['brotherDaughter']
  },
  youngerBrother: {
    father: ['father'],
    mother: ['mother'],
    son: ['brotherSon'],
    daughter: ['brotherDaughter']
  },
  olderSister: {
    father: ['father'],
    mother: ['mother'],
    son: ['sisterSon'],
    daughter: ['sisterDaughter']
  },
  youngerSister: {
    father: ['father'],
    mother: ['mother'],
    son: ['sisterSon'],
    daughter: ['sisterDaughter']
  },
  olderPaternalUncle: {
    father: ['paternalGrandfather'],
    mother: ['paternalGrandmother'],
    son: ['paternalMaleCousinGeneric'],
    daughter: ['paternalFemaleCousinGeneric']
  },
  youngerPaternalUncle: {
    father: ['paternalGrandfather'],
    mother: ['paternalGrandmother'],
    son: ['paternalMaleCousinGeneric'],
    daughter: ['paternalFemaleCousinGeneric']
  },
  paternalAunt: {
    father: ['paternalGrandfather'],
    mother: ['paternalGrandmother'],
    son: ['maternalMaleCousinGeneric'],
    daughter: ['maternalFemaleCousinGeneric']
  },
  maternalUncle: {
    father: ['maternalGrandfather'],
    mother: ['maternalGrandmother'],
    son: ['maternalMaleCousinGeneric'],
    daughter: ['maternalFemaleCousinGeneric']
  },
  maternalAunt: {
    father: ['maternalGrandfather'],
    mother: ['maternalGrandmother'],
    son: ['maternalMaleCousinGeneric'],
    daughter: ['maternalFemaleCousinGeneric']
  },
  husband: {
    wife: ['self']
  },
  wife: {
    husband: ['self']
  }
}

export const relationButtonGroups = [
  {
    title: '常用关系',
    keys: ['father', 'mother', 'olderBrother', 'olderSister', 'youngerBrother', 'youngerSister', 'son', 'daughter']
  },
  {
    title: '配偶与祖辈',
    keys: ['husband', 'wife', 'paternalGrandfather', 'paternalGrandmother', 'maternalGrandfather', 'maternalGrandmother']
  },
  {
    title: '叔伯姨舅',
    keys: ['olderPaternalUncle', 'youngerPaternalUncle', 'paternalAunt', 'maternalUncle', 'maternalAunt']
  }
]

export const sampleExpressions = [
  '妈妈的哥哥的女儿',
  '爸爸的姐姐的儿子',
  '奶奶的儿子的女儿',
  '外婆的女儿的儿子',
  '哥哥的女儿',
  '舅舅的儿子'
]

export const capabilityNotes = [
  '当前优先覆盖常见中文亲属称谓，能处理直系、兄弟姐妹、祖辈、叔伯姑姨舅、侄甥和常见堂表亲。',
  '像“奶奶的儿子”这类天然有歧义的关系，会返回多个可能结果，并在分析中说明原因。',
  '配偶的父母、姻亲、再婚关系和更深层多代关系暂未细化，超出范围时会给出友好提示。'
]

function uniqueStates(states) {
  return [...new Set(states)]
}

function sortStates(states) {
  return [...states].sort((left, right) => {
    const leftOrder = stateMeta[left]?.order ?? 999
    const rightOrder = stateMeta[right]?.order ?? 999
    return leftOrder - rightOrder
  })
}

function summarizeStates(states) {
  const orderedStates = sortStates(uniqueStates(states))
  const titles = orderedStates.map(state => stateMeta[state]?.title || state)
  return {
    states: orderedStates,
    titles,
    display: titles.join(' / ')
  }
}

function normalizeInput(input) {
  return input
    .trim()
    .replace(/\s+/g, '')
    .replace(/[，,、/]+/g, '的')
    .replace(/之/g, '的')
    .replace(/^我的/, '')
    .replace(/^我/, '')
    .replace(/的+/g, '的')
    .replace(/^的|的$/g, '')
}

function parseSegments(input) {
  const normalizedInput = normalizeInput(input)
  if (!normalizedInput) {
    return {
      success: false,
      message: '请输入亲戚关系，例如“妈妈的哥哥的女儿”。'
    }
  }

  const rawSegments = normalizedInput.split('的').filter(Boolean)
  const tokens = []

  for (const raw of rawSegments) {
    const key = aliasLookup.get(raw)
    if (!key) {
      return {
        success: false,
        message: `暂时无法识别“${raw}”这个关系词，请尝试使用更常见的称谓。`,
        normalizedInput,
        tokens
      }
    }
    tokens.push({
      key,
      raw,
      meta: tokenMetaMap[key]
    })
  }

  return {
    success: true,
    normalizedInput,
    tokens
  }
}

function buildStepExplanation(previousSummary, token, nextSummary, matchCount, prevCount) {
  const prefix = `先从“${previousSummary.display}”出发，继续查找“${token.meta.label}”。`
  const matchNote = matchCount < prevCount
    ? '由于并不是所有上一步结果都存在这条关系，因此部分分支被自动排除。'
    : ''
  const ambiguityNote = nextSummary.titles.length > 1
    ? '这一步出现了多种可能，说明仅凭当前关系链还不能唯一锁定到一个人。'
    : '这一步可以明确落到一个具体称谓上。'
  return `${prefix}${token.meta.note}${matchNote}${ambiguityNote} 所以本步得到：${nextSummary.display}。`
}

function buildGraphNodes(steps) {
  const nodes = [
    {
      id: 'self',
      title: '我',
      subtitle: '关系起点',
      tone: 'start'
    }
  ]

  steps.forEach((step, index) => {
    nodes.push({
      id: `step-${index + 1}`,
      title: step.nextTitle,
      subtitle: `输入：${step.tokenLabel}`,
      tone: index === steps.length - 1 ? 'end' : 'normal'
    })
  })

  return nodes
}

function buildFailureResult(parsed, steps, message) {
  return {
    success: false,
    normalizedInput: parsed.normalizedInput,
    tokens: parsed.tokens || [],
    steps,
    finalDisplay: '暂时无法继续推导',
    finalCandidates: [],
    isAmbiguous: false,
    pathText: ['我', ...steps.map(step => step.nextTitle)].join(' -> '),
    graphNodes: buildGraphNodes(steps),
    message
  }
}

export function analyzeKinship(input) {
  const parsed = parseSegments(input)
  if (!parsed.success) {
    return {
      success: false,
      normalizedInput: parsed.normalizedInput || '',
      tokens: parsed.tokens || [],
      steps: [],
      finalDisplay: '暂未得到结果',
      finalCandidates: [],
      isAmbiguous: false,
      pathText: '我',
      graphNodes: [{ id: 'self', title: '我', subtitle: '关系起点', tone: 'start' }],
      message: parsed.message
    }
  }

  let currentStates = ['self']
  const steps = []

  for (const token of parsed.tokens) {
    const previousSummary = summarizeStates(currentStates)
    const nextStateSet = new Set()
    let matchCount = 0

    currentStates.forEach(state => {
      const nextStates = transitions[state]?.[token.key] || []
      if (nextStates.length > 0) {
        matchCount += 1
      }
      nextStates.forEach(nextState => {
        nextStateSet.add(nextState)
      })
    })

    const nextStates = [...nextStateSet]
    if (nextStates.length === 0) {
      const fallbackMessage = `当前规则还不支持从“${previousSummary.display}”继续推导“${token.meta.label}”。`
      return buildFailureResult(parsed, steps, fallbackMessage)
    }

    const nextSummary = summarizeStates(nextStates)
    steps.push({
      index: steps.length + 1,
      rawToken: token.raw,
      tokenKey: token.key,
      tokenLabel: token.meta.label,
      direction: token.meta.direction,
      previousTitle: previousSummary.display,
      nextTitle: nextSummary.display,
      stateCount: nextSummary.states.length,
      relationChange: `${previousSummary.display} -> ${nextSummary.display}`,
      explanation: buildStepExplanation(previousSummary, token, nextSummary, matchCount, currentStates.length)
    })

    currentStates = nextStates
  }

  const finalSummary = summarizeStates(currentStates)
  const isAmbiguous = finalSummary.titles.length > 1
  const finalMessage = isAmbiguous
    ? '这条关系链存在多种可能，页面已把所有合理结果列出来供你判断。'
    : '这条关系链已经能够唯一定位到一个称谓。'

  return {
    success: true,
    normalizedInput: parsed.normalizedInput,
    tokens: parsed.tokens,
    steps,
    finalDisplay: finalSummary.display,
    finalCandidates: finalSummary.titles,
    isAmbiguous,
    pathText: ['我', ...steps.map(step => step.nextTitle)].join(' -> '),
    graphNodes: buildGraphNodes(steps),
    message: finalMessage
  }
}
