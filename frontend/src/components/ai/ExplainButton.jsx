import { useState } from 'react';
import { Sparkles, Loader2, Send } from 'lucide-react';
import { aiApi } from '../../services/api';

const SUGGESTED_QUESTIONS = {
  SIP_STEPUP: [
    'What if I increase my SIP by ₹1,000/month?',
    'How does 6% inflation affect my real returns?',
    'Is a 12% return realistic for Indian markets?',
    'When should I start withdrawing from this SIP?',
  ],
  RETIREMENT: [
    'Will ₹2 Crore be enough to retire?',
    'What if inflation stays above 7%?',
    'Can I retire 5 years earlier?',
    'How much should I keep in equity vs debt?',
  ],
  LOAN_ANALYZE: [
    'Should I take a longer tenure to reduce EMI?',
    'How much interest do I save with prepayment?',
    'Is this a good time to take a home loan in India?',
    'Fixed vs floating rate — which is better now?',
  ],
  LOAN_COMPARE: [
    'Which option is actually cheaper overall?',
    'What processing fees should I watch out for?',
    'Is a shorter tenure worth the higher EMI?',
    'Which lender type is safer — bank or NBFC?',
  ],
  ASSET_ALLOCATION: [
    'Is my current allocation too aggressive?',
    'What are the tax costs of rebalancing?',
    'Should I use index funds for the equity portion?',
    'How often should I rebalance?',
  ],
  CASHFLOW: [
    'Is my savings rate healthy?',
    'Where is the biggest leak in my cashflow?',
    'How big should my emergency fund be?',
    'Where should I park my monthly surplus?',
  ],
  SWP: [
    'Is this withdrawal rate safe long-term?',
    'What if returns drop by 2%?',
    'SWP vs dividend plan — which is tax-efficient?',
    'Should I keep some in debt for safety?',
  ],
  FD: [
    'What is my real return after inflation and tax?',
    'Is FD better than a debt mutual fund right now?',
    'Should I ladder my FDs?',
    'When should I lock in this rate?',
  ],
  RD: [
    'Is an RD better than a SIP in a debt fund?',
    'What is my effective post-tax return?',
    'Should I use RD for short-term goals only?',
    'How does RD compare to SIP for the same goal?',
  ],
  PPF: [
    'Is PPF still worth it vs equity over 15 years?',
    'Should I invest in PPF early in the year?',
    'Can I extend PPF after 15 years?',
    'How does PPF fit with 80C planning?',
  ],
};

const styles = {
  wrap: { marginTop: 16 },
  mainBtn: {
    display: 'inline-flex', alignItems: 'center', gap: 8,
    background: 'linear-gradient(135deg,#7c4dff 0%,#00bcd4 100%)',
    color: '#fff', border: 'none', padding: '10px 18px',
    borderRadius: 10, fontWeight: 600, cursor: 'pointer', fontSize: 14,
  },
  box: {
    marginTop: 14, padding: 16,
    background: 'rgba(124,77,255,0.06)',
    border: '1px solid rgba(124,77,255,0.25)',
    borderRadius: 12,
  },
  label: { fontSize: 12, fontWeight: 600, color: '#7c4dff', marginBottom: 8, letterSpacing: 0.3 },
  body: { fontSize: 13.5, color: 'var(--text)', whiteSpace: 'pre-line', lineHeight: 1.6 },
  pillRow: { display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 12 },
  pill: {
    fontSize: 11.5, background: 'var(--card-bg,#1b2332)', color: '#b8a8ff',
    border: '1px solid rgba(124,77,255,0.35)', padding: '5px 10px',
    borderRadius: 999, cursor: 'pointer',
  },
  inputRow: { display: 'flex', gap: 8, marginTop: 10 },
  input: {
    flex: 1, background: 'var(--card-bg,#1b2332)', border: '1px solid rgba(255,255,255,0.08)',
    color: 'var(--text)', borderRadius: 8, padding: '8px 12px', fontSize: 13,
  },
  sendBtn: {
    background: '#7c4dff', color: '#fff', border: 'none', padding: '8px 14px',
    borderRadius: 8, cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13,
  },
  followBox: {
    marginTop: 12, padding: 14,
    background: 'rgba(0,188,212,0.06)',
    border: '1px solid rgba(0,188,212,0.25)',
    borderRadius: 10,
  },
  followLabel: { fontSize: 11, fontWeight: 700, color: '#00bcd4', marginBottom: 6 },
  quota: { fontSize: 11, color: 'var(--text-muted)', marginTop: 8 },
  err: { color: '#ff4c6a', fontSize: 13, marginTop: 10 },
};

export default function ExplainButton({ type, inputs, result, disabled }) {
  const [loading, setLoading] = useState(false);
  const [explanation, setExplanation] = useState('');
  const [remaining, setRemaining] = useState(null);
  const [error, setError] = useState('');
  const [followUp, setFollowUp] = useState('');
  const [followAnswer, setFollowAnswer] = useState('');
  const [followLoading, setFollowLoading] = useState(false);

  const suggestions = SUGGESTED_QUESTIONS[type] || [];

  const errMsg = (e) => {
    if (e?.response?.status === 429) {
      const d = e.response.data || {};
      return `You've hit the AI limit (${d.limit || 10}/hour). Try again in ~${Math.ceil((d.retryAfterSeconds || 60) / 60)} min.`;
    }
    return 'AI is unavailable right now. Please try again.';
  };

  const explain = async () => {
    if (!result) return;
    setLoading(true); setError(''); setExplanation(''); setFollowAnswer('');
    try {
      const res = await aiApi.explainCalculator(type, inputs, result);
      const data = res.data.data;
      setExplanation(data.explanation);
      setRemaining(data.remainingRequests);
    } catch (e) {
      setError(errMsg(e));
    }
    setLoading(false);
  };

  const askFollowUp = async (question) => {
    const q = (question || '').trim();
    if (!q) return;
    setFollowLoading(true); setError(''); setFollowAnswer('');
    try {
      const res = await aiApi.followup('CALCULATOR', explanation, q);
      const data = res.data.data;
      setFollowAnswer(data.explanation);
      setRemaining(data.remainingRequests);
    } catch (e) {
      setError(errMsg(e));
    }
    setFollowLoading(false);
  };

  return (
    <div style={styles.wrap}>
      <button style={styles.mainBtn} onClick={explain} disabled={loading || disabled}>
        {loading ? <Loader2 size={16} className="spin" /> : <Sparkles size={16} />}
        {loading ? 'Analysing with AI...' : 'Explain this result with AI'}
      </button>

      {error && <div style={styles.err}>{error}</div>}

      {explanation && (
        <div style={styles.box}>
          <div style={styles.label}>MoneyMatters AI</div>
          <div style={styles.body}>{explanation}</div>
          {remaining != null && (
            <div style={styles.quota}>{remaining} AI requests left this hour</div>
          )}

          {suggestions.length > 0 && (
            <>
              <div style={{ ...styles.label, marginTop: 14 }}>Ask a follow-up</div>
              <div style={styles.pillRow}>
                {suggestions.map((q) => (
                  <button key={q} style={styles.pill}
                    onClick={() => { setFollowUp(q); askFollowUp(q); }}>
                    {q}
                  </button>
                ))}
              </div>
            </>
          )}

          <div style={styles.inputRow}>
            <input style={styles.input} type="text" value={followUp}
              onChange={(e) => setFollowUp(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && askFollowUp(followUp)}
              placeholder="Ask your own question..." />
            <button style={styles.sendBtn} onClick={() => askFollowUp(followUp)} disabled={followLoading}>
              {followLoading ? <Loader2 size={14} className="spin" /> : <Send size={14} />} Ask
            </button>
          </div>

          {followAnswer && (
            <div style={styles.followBox}>
              <div style={styles.followLabel}>AI ANSWER</div>
              <div style={styles.body}>{followAnswer}</div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
