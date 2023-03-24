import torch
print(torch.version.cuda) # 10.2
print(torch.cuda.device_count())
print(torch.cuda.is_available()) # True
print(torch.ones(1).cuda())
max_memory_per_gpu_in_bytes = torch.cuda.mem_get_info(0)
print(max_memory_per_gpu_in_bytes)
