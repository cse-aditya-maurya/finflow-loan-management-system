import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createApplication, getMyApplications, submitApplication } from '../../api/applications';
import api from '../../api/axios';

vi.mock('../../api/axios');

describe('Applications API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('creates application', async () => {
    const mockData = { loanType: 'Personal', amount: 50000 };
    vi.mocked(api.post).mockResolvedValue({ data: { id: 1, ...mockData } } as any);

    const result = await createApplication(mockData as any);

    expect(api.post).toHaveBeenCalledWith('/applications', mockData);
    expect(result.id).toBe(1);
  });

  it('fetches user applications', async () => {
    const mockApps = [{ id: 1, status: 'SUBMITTED' }];
    vi.mocked(api.get).mockResolvedValue({ data: mockApps } as any);

    const result = await getMyApplications();

    expect(api.get).toHaveBeenCalledWith('/applications/my');
    expect(result).toEqual(mockApps);
  });

  it('submits application', async () => {
    vi.mocked(api.post).mockResolvedValue({ data: { status: 'SUBMITTED' } } as any);

    const result = await submitApplication(1);

    expect(api.post).toHaveBeenCalledWith('/applications/1/submit');
    expect(result.status).toBe('SUBMITTED');
  });
});
