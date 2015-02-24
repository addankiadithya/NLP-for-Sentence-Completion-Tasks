README.TXT
------------

Environment: 
-------------

To run the files JDK6+ should be installed. 

	Execution Instructions:
	-----------------------

	Change to the Directory the Code files and jar files are in, then:
	For Compiling:

		I:\RM\MP_RM>javac -cp ".;*" *.java
			Note: Some input files use or override a deprecated API.
			Note: Recompile with -Xlint:deprecation for details.

	For Running the Models:

		I:\RM\MP_RM>java -cp GREBackoffModelMSWebService
		I:\RM\MP_RM>java -cp [OtherModelFileNames]

Dataset:
--------- 

1)Training and testing data taken from the website http://research.microsoft.com/en-us/projects/scc/ is  organised into folders "Holmes_Training_Data" and  "testing_data" . These folders have been assumed to be present in the same folder 
as the source code files. If not, path where the  dataset is taken from  has to be changed in the code. The files "Holmes.machine_format.questions.txt" and  "Holmes.machine_format.answers.txt" are used for testing and are considered as the gold standard.

2) Microsoft Web Service is used to obtain the N-gram probabilities in Microsoft Web Corpus. For using Microsoft Web Services, authentication token is required which can be obtained from the website http://web-ngram.research.microsoft.com/  

3) For using WordNet, API jar taken from edu.mit.jwi needs to be used and for using Latent Semantic Analysis code, API jar downloaded from http://math.nist.gov/javanumerics/jama/ needs to be used

Files included:
----------------
 
GREBackwardBigramModelHolmesCorpus - Language model  implementation using backward bigram  probabilities as features. This model is trained using novels corpus. 

GREForwardBackwardBigramModelHolmesCorpus - Language model implementation using backward and forward bigram  probabilities as features. This model is trained using novels corpus. 

GREBackoffModelMSWebService - Language model implementation  using back off method for smoothing. This model is trained using Microsoft Web N-gram corpus.

GREBackoffModelMSWebService_RootWord - Language model implementation  using back off method for smoothing. This model is trained using Microsoft Web N-gram corpus.Additionally, root words are also considered as features.
 
GREWeightedSumModelMSWebService - Language model implementation  using weighted sum of N-gram probabilities as features. This model is trained using Microsoft Web N-gram corpus.

GRENonWeightedSumModelMSWebService - Language model implementation  using non weighted sum of N-gram probabilities as features. This model is trained using Microsoft Web N-gram corpus.

GREBackwardBigramModelUsingMSWebService - Language model implementation  using backward bigram probabilities as features. This model is trained using Microsoft Web N-gram corpus.

GREBackwardTrigramModelUsingMSWebService - Language model implementation  using backward trigram probabilities as features. This model is trained using Microsoft Web N-gram corpus.

GREBackwardFourgramModelUsingMSWebService - Language model implementation  using backward fourgram probabilities as features. This model is trained using Microsoft Web N-gram corpus.

GRELSAUsingHolmesCorpus- Latent Semantic Analysis implementation using Singular Value Decomposition. This model is trained using novels corpus.
